package com.guvi.honey_pot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.honey_pot.model.FinalResultCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CallbackService {

    @Value("${honeypot.guvi-callback-url}")
    private String callbackUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendFinalResult(FinalResultCallback callback) {
        try {
            String jsonBody = objectMapper.writeValueAsString(callback);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(callbackUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Callback sent successfully: " + response.statusCode());

        } catch (Exception e) {
            System.err.println("Failed to send callback: " + e.getMessage());
        }
    }
}
