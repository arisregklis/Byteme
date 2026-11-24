// Το παρακάτω κομμάτι κώδικα γράφτηκε από εργαλέιο ΠΤΝ (Claude, model: Opus5.5 medium, tokens ~ 6500)
package gr.aueb.byteme.common;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

// Βοηθητικές μέθοδοι HTTP:
//  - server: διαβάζουμε το αίτημα που ήρθε και απαντάμε με JSON
//  - client: καλούμε άλλο σύστημα με GET / POST
//
//     Http http = new Http();
//     http.sendJson(exchange, 200, "{\"accepted\": true}");
//     HttpResult answer = http.post("http://localhost:3000/requests", json);
public class HTTP {
    private static final String JSON_TYPE = "application/json; charset=UTF-8";
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final HttpClient client = HttpClient.newHttpClient();

    // ---------- server ----------

    public String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    public void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", JSON_TYPE);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    // Σφάλμα στη μορφή του API.md §0.5: code, message, timestamp
    public void sendError(HttpExchange exchange, int status, String code, String message)
            throws IOException {
        String json = "{\"code\": \"" + code + "\", "
                + "\"message\": \"" + clean(message) + "\", "
                + "\"timestamp\": \"" + now() + "\"}";
        sendJson(exchange, status, json);
    }

    // π.χ. 2026-11-23T18:30:00Z (UTC, ISO-8601)
    public String now() {
        return Instant.now().toString();
    }

    // Τα " μέσα στο μήνυμα θα χαλούσαν το JSON, οπότε γίνονται '
    private String clean(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('"', '\'');
    }

    

    public HTTPResult get(String url) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .GET()
                .build();
        return send(request, url);
    }

    public HTTPResult post(String url, String json) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", JSON_TYPE)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request, url);
    }

    // Αν το άλλο σύστημα είναι κλειστό ή δεν απαντά μέσα στο TIMEOUT -> "503"
    private HTTPResult send(HttpRequest request, String url) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return new HTTPResult(response.statusCode(), response.body());
        } catch (Exception e) {
            throw new IllegalStateException("503: cannot reach " + url);
        }
    }
}

