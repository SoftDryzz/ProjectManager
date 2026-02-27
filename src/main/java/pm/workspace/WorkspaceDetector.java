package pm.workspace;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import pm.detector.ProjectType;
import pm.detector.ProjectTypeDetector;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Detects workspace/monorepo modules within a project.
 * Supports Cargo workspaces, npm/pnpm/yarn workspaces, Gradle multi-project, and Go multi-module.
 */
public final class WorkspaceDetector {

    private static final Gson GSON = new Gson();

    private WorkspaceDetector() {
        throw new AssertionError("Utility class");
    }

    /**
     * Detects workspace modules for the given project.
     *
     * @param primaryType the primary project type
     * @param projectRoot the project root directory
     * @return list of detected modules (may be empty, never null)
     */
    public static List<WorkspaceModule> detect(ProjectType primaryType, Path projectRoot) {
        if (primaryType == null || projectRoot == null || !Files.isDirectory(projectRoot)) {
            return List.of();
        }

        return switch (primaryType) {
            case RUST -> detectCargoWorkspace(projectRoot);
            case NODEJS, PNPM, BUN, YARN -> detectNpmWorkspace(projectRoot);
            case GRADLE -> detectGradleMultiProject(projectRoot);
            case GO -> detectGoModules(projectRoot);
            default -> List.of();
        };
    }

    // ============================================================
    // Cargo workspaces
    // ============================================================

    /**
     * Detects Cargo workspace members from Cargo.toml.
     * Looks for [workspace] section with members list.
     */
    static List<WorkspaceModule> detectCargoWorkspace(Path projectRoot) {
        Path cargoToml = projectRoot.resolve(Constants.FILE_CARGO_TOML);
        if (!Files.isRegularFile(cargoToml)) {
            return List.of();
        }

        try {
            String content = Files.readString(cargoToml);
            if (!content.contains("[workspace]")) {
                return List.of();
            }

            List<String> members = parseCargoMembers(content);
            List<WorkspaceModule> modules = new ArrayList<>();

            for (String member : members) {
                Path memberPath = projectRoot.resolve(member);
                if (Files.isDirectory(memberPath)) {
                    String name = memberPath.getFileName().toString();
                    ProjectType type = safeDetect(memberPath);
                    modules.add(new WorkspaceModule(name, member, type));
                }
            }

            return modules;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Parses member paths from Cargo.toml workspace section.
     * Handles: members = ["app", "lib", "tools/helper"]
     */
    static List<String> parseCargoMembers(String content) {
        List<String> members = new ArrayList<>();
        Pattern pattern = Pattern.compile("members\\s*=\\s*\\[([^]]*)]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String membersBlock = matcher.group(1);
            Pattern quotedPattern = Pattern.compile("\"([^\"]+)\"");
            Matcher quotedMatcher = quotedPattern.matcher(membersBlock);
            while (quotedMatcher.find()) {
                members.add(quotedMatcher.group(1));
            }
        }

        return members;
    }

    // ============================================================
    // npm/pnpm/yarn workspaces
    // ============================================================

    /**
     * Detects npm workspace packages from package.json.
     * Handles both array format and object format with "packages" key.
     */
    static List<WorkspaceModule> detectNpmWorkspace(Path projectRoot) {
        Path packageJson = projectRoot.resolve(Constants.FILE_PACKAGE_JSON);
        if (!Files.isRegularFile(packageJson)) {
            return List.of();
        }

        try {
            String content = Files.readString(packageJson);
            JsonObject json = GSON.fromJson(content, JsonObject.class);

            if (json == null || !json.has("workspaces")) {
                return List.of();
            }

            List<String> workspacePatterns = parseNpmWorkspaces(json);
            List<WorkspaceModule> modules = new ArrayList<>();

            for (String pattern : workspacePatterns) {
                if (pattern.endsWith("/*")) {
                    // Glob pattern: expand to subdirectories
                    String parentDir = pattern.substring(0, pattern.length() - 2);
                    Path parent = projectRoot.resolve(parentDir);
                    if (Files.isDirectory(parent)) {
                        try (Stream<Path> dirs = Files.list(parent)) {
                            dirs.filter(Files::isDirectory).forEach(dir -> {
                                String name = dir.getFileName().toString();
                                String relativePath = parentDir + "/" + name;
                                ProjectType type = safeDetect(dir);
                                modules.add(new WorkspaceModule(name, relativePath, type));
                            });
                        }
                    }
                } else {
                    // Direct path
                    Path modulePath = projectRoot.resolve(pattern);
                    if (Files.isDirectory(modulePath)) {
                        String name = modulePath.getFileName().toString();
                        ProjectType type = safeDetect(modulePath);
                        modules.add(new WorkspaceModule(name, pattern, type));
                    }
                }
            }

            return modules;
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Parses workspace patterns from package.json.
     * Handles: "workspaces": ["packages/*"] and "workspaces": {"packages": ["packages/*"]}
     */
    static List<String> parseNpmWorkspaces(JsonObject json) {
        List<String> patterns = new ArrayList<>();
        JsonElement workspaces = json.get("workspaces");

        if (workspaces.isJsonArray()) {
            JsonArray arr = workspaces.getAsJsonArray();
            for (JsonElement el : arr) {
                if (el.isJsonPrimitive()) {
                    patterns.add(el.getAsString());
                }
            }
        } else if (workspaces.isJsonObject()) {
            JsonObject obj = workspaces.getAsJsonObject();
            if (obj.has("packages") && obj.get("packages").isJsonArray()) {
                for (JsonElement el : obj.getAsJsonArray("packages")) {
                    if (el.isJsonPrimitive()) {
                        patterns.add(el.getAsString());
                    }
                }
            }
        }

        return patterns;
    }

    // ============================================================
    // Gradle multi-project
    // ============================================================

    /**
     * Detects Gradle sub-projects from settings.gradle or settings.gradle.kts.
     */
    static List<WorkspaceModule> detectGradleMultiProject(Path projectRoot) {
        Path settingsGroovy = projectRoot.resolve(Constants.FILE_SETTINGS_GRADLE);
        Path settingsKts = projectRoot.resolve(Constants.FILE_SETTINGS_GRADLE_KTS);

        Path settingsFile = Files.isRegularFile(settingsGroovy) ? settingsGroovy :
                Files.isRegularFile(settingsKts) ? settingsKts : null;

        if (settingsFile == null) {
            return List.of();
        }

        try {
            String content = Files.readString(settingsFile);
            List<String> includes = parseGradleIncludes(content);
            List<WorkspaceModule> modules = new ArrayList<>();

            for (String include : includes) {
                // Convert Gradle notation :app:sub to app/sub
                String relativePath = include.replace(":", "/");
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }

                Path modulePath = projectRoot.resolve(relativePath);
                if (Files.isDirectory(modulePath)) {
                    String name = modulePath.getFileName().toString();
                    ProjectType type = safeDetect(modulePath);
                    modules.add(new WorkspaceModule(name, relativePath, type));
                }
            }

            return modules;
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Parses include directives from Gradle settings files.
     * Handles: include("app", "lib"), include 'app', 'lib', include(":app")
     */
    static List<String> parseGradleIncludes(String content) {
        List<String> includes = new ArrayList<>();

        // Match include("app", "lib") or include('app', 'lib') or include "app", "lib"
        Pattern pattern = Pattern.compile("include[\\s(]+([^)\\n]+)[)\\n]?");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String args = matcher.group(1);
            // Extract quoted strings
            Pattern quotedPattern = Pattern.compile("[\"']([^\"']+)[\"']");
            Matcher quotedMatcher = quotedPattern.matcher(args);
            while (quotedMatcher.find()) {
                String module = quotedMatcher.group(1);
                // Remove leading colon (Gradle notation)
                if (module.startsWith(":")) {
                    module = module.substring(1);
                }
                includes.add(module);
            }
        }

        return includes;
    }

    // ============================================================
    // Go multi-module
    // ============================================================

    /**
     * Detects Go sub-modules by finding go.mod files in subdirectories.
     */
    static List<WorkspaceModule> detectGoModules(Path projectRoot) {
        try (Stream<Path> walk = Files.walk(projectRoot, 3)) {
            List<WorkspaceModule> modules = new ArrayList<>();

            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals("go.mod"))
                    .filter(p -> !p.getParent().equals(projectRoot))
                    .forEach(goMod -> {
                        Path moduleDir = goMod.getParent();
                        String name = moduleDir.getFileName().toString();
                        String relativePath = projectRoot.relativize(moduleDir).toString().replace("\\", "/");
                        modules.add(new WorkspaceModule(name, relativePath, ProjectType.GO));
                    });

            return modules;
        } catch (IOException e) {
            return List.of();
        }
    }

    // ============================================================
    // Helpers
    // ============================================================

    /**
     * Safely detects a project type, returning UNKNOWN on any error.
     */
    private static ProjectType safeDetect(Path path) {
        try {
            return ProjectTypeDetector.detect(path);
        } catch (Exception e) {
            return ProjectType.UNKNOWN;
        }
    }
}
