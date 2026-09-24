package pm.repos;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** Minimal fake of the GitHub API on 127.0.0.1, for tests. */
final class FakeGitHub implements AutoCloseable {

    record Reply(int status, String body, Map<String, String> headers, long delayMs) {
        static Reply json(String body) {
            return new Reply(200, body, Map.of(), 0);
        }

        static Reply status(int status) {
            return new Reply(status, "{\"message\":\"status " + status + "\"}", Map.of(), 0);
        }

        Reply header(String name, String value) {
            Map<String, String> copy = new HashMap<>(headers);
            copy.put(name, value);
            return new Reply(status, body, copy, delayMs);
        }

        Reply delay(long millis) {
            return new Reply(status, body, headers, millis);
        }
    }

    private final HttpServer server;
    private final Map<String, Reply> routes = new ConcurrentHashMap<>();
    private final List<String> requests = new CopyOnWriteArrayList<>();
    private final List<String> authorizations = new CopyOnWriteArrayList<>();

    FakeGitHub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
    }

    static String fixture(String name) {
        try (InputStream in = FakeGitHub.class.getResourceAsStream("/github/" + name)) {
            if (in == null) {
                throw new IllegalArgumentException("missing fixture " + name);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    String baseUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    void on(String pathAndQuery, Reply reply) {
        routes.put(pathAndQuery, reply);
    }

    List<String> requests() {
        return requests;
    }

    List<String> authorizations() {
        return authorizations;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        String key = exchange.getRequestURI().getRawPath() + (query == null ? "" : "?" + query);
        requests.add(key);
        authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst("Authorization")));
        Reply reply = routes.getOrDefault(key, Reply.status(404));
        if (reply.delayMs() > 0) {
            try {
                Thread.sleep(reply.delayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        byte[] bytes = reply.body().getBytes(StandardCharsets.UTF_8);
        reply.headers().forEach((name, value) -> exchange.getResponseHeaders().add(name, value));
        exchange.sendResponseHeaders(reply.status(), bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            } catch (IOException ignored) {
                // client gave up (timeout tests)
            }
        }
        exchange.close();
    }

    private boolean closed;

    /** Idempotent: tests may close the server early to simulate being offline. */
    @Override
    public synchronized void close() {
        if (!closed) {
            closed = true;
            server.stop(0);
        }
    }
}
