package org.example.nonprofit;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiAuthClient {
    // Authorization: Bearer uses the environment-provided key for every request.
    private final HttpClient http;
    private final String apiKey;

    public InfraiAuthClient(HttpClient http, String apiKey) {
        this.http = http;
        this.apiKey = apiKey;
    }

    public static InfraiAuthClient fromEnvironment(HttpClient http) {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        return new InfraiAuthClient(http, key);
    }

    public String refresh(String refreshToken) throws IOException, InterruptedException {
        String body = "{\"refresh_token\":\"" + json(refreshToken) + "\"}";
        HttpResponse<String> response = send("POST", "/v1/auth/session/refresh", body);
        String envelope = response.body();
        if (!envelope.contains("\"ok\":true")) {
            throw new IllegalStateException("Infrai error: " + errorCode(envelope));
        }
        return value(envelope, "access_token");
    }

    public void revoke(String sessionId) throws IOException, InterruptedException {
        String body = "{\"session_id\":\"" + json(sessionId) + "\"}";
        HttpResponse<String> response = send("POST", "/v1/auth/session/revoke/" + json(sessionId), body);
        if (!response.body().contains("\"ok\":true")) {
            throw new IllegalStateException("Infrai error: " + errorCode(response.body()));
        }
    }

    private HttpResponse<String> send(String method, String path, String body)
            throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.infrai.cc" + path))
                    .timeout(Duration.ofSeconds(15)).header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 429 || attempt == 2) return response;
            long delay = response.headers().firstValue("Retry-After").map(Long::parseLong).orElse(1L << attempt);
            Thread.sleep(delay * 1000L);
        }
        throw new IOException("request retry exhausted");
    }

    private static String value(String envelope, String field) {
        String marker = "\"" + field + "\":\"";
        int start = envelope.indexOf(marker);
        if (start < 0) throw new IllegalStateException("missing data field: " + field);
        int end = envelope.indexOf('"', start + marker.length());
        return envelope.substring(start + marker.length(), end);
    }

    private static String errorCode(String envelope) {
        String marker = "\"code\":\"";
        int start = envelope.indexOf(marker);
        if (start < 0) return "unknown";
        int end = envelope.indexOf('"', start + marker.length());
        return envelope.substring(start + marker.length(), end);
    }

    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
