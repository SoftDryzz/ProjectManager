package pm.repos;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import pm.util.Constants;
import pm.util.UpdateChecker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal GitHub REST client for {@code pm repos}.
 *
 * <p>Security (spec S1-S3, S8, S9): redirects are never followed; every
 * request, including pagination links, must go to the base URL's scheme, host
 * and port; the base URL must be https (http only on loopback, for tests);
 * the token is only sent in the Authorization header; path segments are
 * validated and encoded; bodies are capped and parsed into fixed DTOs.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class GitHubClient {

    public static final String DEFAULT_BASE_URL = "https://api.github.com";
    static final int MAX_PAGES = 20;
    static final int DEFAULT_TIMEOUT_MS = 10_000;
    static final int DEFAULT_MAX_BODY_BYTES = 10 * 1024 * 1024;

    private static final Pattern LOGIN = Pattern.compile("[A-Za-z0-9](?:-?[A-Za-z0-9]){0,38}");
    private static final Pattern REPO_PART = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    private static final Pattern NEXT_LINK = Pattern.compile("<([^>]+)>\\s*;\\s*rel=\"next\"");
    private static final Gson GSON = new Gson();

    private final URI base;
    private final Token token;
    private final int timeoutMs;
    private final int maxBodyBytes;

    /** @param token may be null (anonymous requests, public data only) */
    public GitHubClient(String baseUrl, Token token) {
        this(baseUrl, token, DEFAULT_TIMEOUT_MS, DEFAULT_MAX_BODY_BYTES);
    }

    GitHubClient(String baseUrl, Token token, int timeoutMs, int maxBodyBytes) {
        String trimmed = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        URI uri = URI.create(trimmed);
        if (!isAllowedBase(uri)) {
            // the URL itself is not echoed: it may carry credentials
            throw new IllegalArgumentException(
                    "GitHub API URL must use https (http only on loopback) and contain no credentials");
        }
        this.base = uri;
        this.token = token;
        this.timeoutMs = timeoutMs;
        this.maxBodyBytes = maxBodyBytes;
    }

    public boolean isAuthenticated() {
        return token != null;
    }

    /** GitHub's login format; anything else is rejected before building a URL. */
    public static boolean isValidLogin(String login) {
        return login != null && LOGIN.matcher(login).matches();
    }

    /** Login of the token's owner. */
    public String currentLogin() throws GitHubException {
        UserJson user = parse(get(resolve("/user")).body(), UserJson.class);
        if (user == null || user.login == null) {
            throw new GitHubException(GitHubException.Kind.BAD_RESPONSE, "GitHub did not return a login", null, null);
        }
        return user.login;
    }

    /** Repos the user owns, can access through organizations, or collaborates on. */
    public Listing myRepos() throws GitHubException {
        return listRepos(resolve("/user/repos?affiliation=owner,organization_member,collaborator&per_page=100&sort=pushed"));
    }

    /** Public repos owned by an account (works without a token). */
    public Listing publicRepos(String login) throws GitHubException {
        if (!isValidLogin(login)) {
            throw new IllegalArgumentException("Invalid GitHub login");
        }
        return listRepos(resolve("/users/" + login + "/repos?type=owner&per_page=100&sort=pushed"));
    }

    /** Collaborators of a repo; GitHub requires push access. */
    public List<Collaborator> collaborators(String owner, String repo) throws GitHubException {
        if (!isValidPart(owner) || !isValidPart(repo)) {
            throw new IllegalArgumentException("Invalid repository name");
        }
        List<Collaborator> result = new ArrayList<>();
        URI next = resolve("/repos/" + encode(owner) + "/" + encode(repo) + "/collaborators?per_page=100");
        for (int page = 0; next != null && page < MAX_PAGES; page++) {
            Response response = get(next);
            CollaboratorJson[] items = parse(response.body(), CollaboratorJson[].class);
            if (items != null) {
                for (CollaboratorJson item : items) {
                    if (item != null && item.login != null) {
                        result.add(new Collaborator(item.login, item.roleName == null ? "" : item.roleName));
                    }
                }
            }
            next = response.next();
        }
        return List.copyOf(result);
    }

    // ------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------

    private Listing listRepos(URI first) throws GitHubException {
        List<RemoteRepo> repos = new ArrayList<>();
        int ssoHidden = 0;
        URI next = first;
        int pages = 0;
        while (next != null && pages < MAX_PAGES) {
            Response response = get(next);
            RepoJson[] items = parse(response.body(), RepoJson[].class);
            if (items == null) {
                throw new GitHubException(GitHubException.Kind.BAD_RESPONSE, "GitHub did not return a list", null, null);
            }
            for (RepoJson item : items) {
                RemoteRepo repo = toRemote(item);
                if (repo != null) {
                    repos.add(repo);
                }
            }
            ssoHidden = Math.max(ssoHidden, ssoOrganizationCount(response.sso()));
            next = response.next();
            pages++;
        }
        return new Listing(List.copyOf(repos), next != null, ssoHidden);
    }

    private Response get(URI uri) throws GitHubException {
        if (!sameOrigin(base, uri)) {
            throw new GitHubException(GitHubException.Kind.REFUSED,
                    "Refused to send a request outside the GitHub API", null, null);
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            conn.setRequestProperty("User-Agent", "ProjectManager/" + Constants.VERSION);
            if (token != null) {
                conn.setRequestProperty("Authorization", "Bearer " + token.value());
            }
            int code = conn.getResponseCode();
            if (code != 200) {
                throw errorFor(code, conn);
            }
            String body = readLimited(conn.getInputStream());
            return new Response(body, nextLink(conn.getHeaderField("Link")), conn.getHeaderField("X-GitHub-SSO"));
        } catch (IOException e) {
            throw new GitHubException(GitHubException.Kind.NETWORK, UpdateChecker.describeNetworkError(e), null, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readLimited(InputStream in) throws IOException, GitHubException {
        try (InputStream stream = in) {
            byte[] data = stream.readNBytes(maxBodyBytes + 1);
            if (data.length > maxBodyBytes) {
                throw new GitHubException(GitHubException.Kind.BAD_RESPONSE, "GitHub response too large", null, null);
            }
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private static GitHubException errorFor(int code, HttpURLConnection conn) {
        if (code == 401) {
            return new GitHubException(GitHubException.Kind.UNAUTHORIZED, "GitHub rejected the token (HTTP 401)", null, null);
        }
        if (code == 403 || code == 429) {
            Instant retryAt = rateLimitReset(conn.getHeaderField("X-RateLimit-Remaining"),
                    conn.getHeaderField("X-RateLimit-Reset"), conn.getHeaderField("Retry-After"), Instant.now());
            if (retryAt != null || code == 429) {
                return new GitHubException(GitHubException.Kind.RATE_LIMITED, "GitHub rate limit reached", retryAt, null);
            }
            return new GitHubException(GitHubException.Kind.FORBIDDEN, "GitHub denied access (HTTP 403)", null, null);
        }
        if (code == 404) {
            return new GitHubException(GitHubException.Kind.NOT_FOUND, "Not found on GitHub (HTTP 404)", null, null);
        }
        if (code >= 300 && code < 400) {
            return new GitHubException(GitHubException.Kind.BAD_RESPONSE,
                    "Unexpected redirect from GitHub (HTTP " + code + ")", null, null);
        }
        return new GitHubException(GitHubException.Kind.BAD_RESPONSE,
                "Unexpected response from GitHub (HTTP " + code + ")", null, null);
    }

    static Instant rateLimitReset(String remaining, String reset, String retryAfter, Instant now) {
        try {
            if (retryAfter != null) {
                return now.plusSeconds(Long.parseLong(retryAfter.trim()));
            }
            if (remaining != null && remaining.trim().equals("0") && reset != null) {
                return Instant.ofEpochSecond(Long.parseLong(reset.trim()));
            }
        } catch (NumberFormatException ignored) {
            // malformed header: treat as not rate limited
        }
        return null;
    }

    static URI nextLink(String header) {
        if (header == null) {
            return null;
        }
        Matcher m = NEXT_LINK.matcher(header);
        if (!m.find()) {
            return null;
        }
        try {
            return URI.create(m.group(1));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static int ssoOrganizationCount(String header) {
        if (header == null || !header.contains("partial-results")) {
            return 0;
        }
        int idx = header.indexOf("organizations=");
        if (idx < 0) {
            return 0;
        }
        String list = header.substring(idx + "organizations=".length());
        int end = list.indexOf(';');
        if (end >= 0) {
            list = list.substring(0, end);
        }
        return (int) Arrays.stream(list.split(",")).map(String::trim).filter(s -> !s.isEmpty()).count();
    }

    /** Spec S1: same scheme, host (ignoring case) and port as the base URL, and no userinfo. */
    static boolean sameOrigin(URI base, URI uri) {
        return uri.getScheme() != null && uri.getScheme().equalsIgnoreCase(base.getScheme())
                && uri.getHost() != null && uri.getHost().equalsIgnoreCase(base.getHost())
                && uri.getUserInfo() == null
                && effectivePort(uri) == effectivePort(base);
    }

    private static int effectivePort(URI uri) {
        if (uri.getPort() != -1) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private static boolean isAllowedBase(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (host == null || scheme == null || uri.getRawUserInfo() != null) {
            return false;
        }
        if (scheme.equalsIgnoreCase("https")) {
            return true;
        }
        return scheme.equalsIgnoreCase("http")
                && (host.equals("127.0.0.1") || host.equalsIgnoreCase("localhost") || host.equals("[::1]"));
    }

    private URI resolve(String pathAndQuery) {
        return URI.create(base + pathAndQuery);
    }

    private static boolean isValidPart(String part) {
        return part != null && REPO_PART.matcher(part).matches() && !part.equals(".") && !part.equals("..");
    }

    private static String encode(String part) {
        return URLEncoder.encode(part, StandardCharsets.UTF_8);
    }

    private static <T> T parse(String body, Class<T> type) throws GitHubException {
        try {
            return GSON.fromJson(body, type);
        } catch (JsonParseException e) {
            throw new GitHubException(GitHubException.Kind.BAD_RESPONSE, "Could not read GitHub's response", null, e);
        }
    }

    private static RemoteRepo toRemote(RepoJson json) {
        if (json == null || json.name == null || json.owner == null || json.owner.login == null) {
            return null;
        }
        return new RemoteRepo(json.owner.login, "Organization".equals(json.owner.type), json.name,
                json.isPrivate, json.description, json.defaultBranch, parseInstant(json.pushedAt),
                json.archived, json.fork, json.htmlUrl, permission(json.permissions));
    }

    private static String permission(PermissionsJson p) {
        if (p == null) {
            return null;
        }
        if (p.admin) {
            return "admin";
        }
        if (p.maintain) {
            return "maintain";
        }
        if (p.push) {
            return "write";
        }
        if (p.triage) {
            return "triage";
        }
        return p.pull ? "read" : null;
    }

    private static Instant parseInstant(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private record Response(String body, URI next, String sso) {
    }

    private static final class UserJson {
        String login;
    }

    private static final class OwnerJson {
        String login;
        String type;
    }

    private static final class PermissionsJson {
        boolean admin;
        boolean maintain;
        boolean push;
        boolean triage;
        boolean pull;
    }

    private static final class RepoJson {
        String name;
        OwnerJson owner;
        @SerializedName("private")
        boolean isPrivate;
        String description;
        @SerializedName("default_branch")
        String defaultBranch;
        @SerializedName("pushed_at")
        String pushedAt;
        boolean archived;
        boolean fork;
        @SerializedName("html_url")
        String htmlUrl;
        PermissionsJson permissions;
    }

    private static final class CollaboratorJson {
        String login;
        @SerializedName("role_name")
        String roleName;
    }
}
