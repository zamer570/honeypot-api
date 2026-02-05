package com.guvi.honey_pot.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.guvi.honey_pot.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class AIAgentService {

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final List<String> PERSONAS = Arrays.asList(
            "elderly person unfamiliar with technology",
            "middle-aged person worried about account security",
            "young professional cautious but cooperative",
            "confused individual seeking clarification"
    );

    public String generateResponse(String scammerMessage, List<Message> history,
                                   String persona, boolean scamDetected) {

        String systemPrompt = buildSystemPrompt(persona, scamDetected);
        String conversationContext = buildConversationContext(history, scammerMessage);

    try {
        String combinedPrompt = systemPrompt + "\n\n" + conversationContext;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", combinedPrompt)
                ))
        ));

        requestBody.put("generationConfig", Map.of(
                "maxOutputTokens", 200,
                "temperature", 0.7
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(
                        objectMapper.writeValueAsString(requestBody)))
                .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            Map<String, Object> responseMap = objectMapper.readValue(
                    response.body(), Map.class);

            // Parse Gemini response structure
            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) responseMap.get("candidates");
            Map<String, Object> content =
                    (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            // Fallback to rule-based response
            return generateFallbackResponse(scammerMessage, scamDetected);
        }
    }

    private String buildSystemPrompt(String persona, boolean scamDetected) {
        if (!scamDetected) {
            return "You are a helpful assistant responding naturally to messages.";
        }

        return String.format(
                "You are roleplaying as %s engaging with a potential scammer. " +
                        "Your goal is to: " +
                        "1. Appear genuinely concerned and cooperative " +
                        "2. Ask clarifying questions to extract information " +
                        "3. Never reveal you know it's a scam " +
                        "4. Gradually show willingness to help while seeking details " +
                        "5. Ask for bank details, UPI IDs, links, or phone numbers naturally " +
                        "Keep responses short (1-3 sentences), realistic, and human-like. " +
                        "Show slight confusion or concern to encourage the scammer to provide more details.",
                persona
        );
    }

    private String buildConversationContext(List<Message> history, String newMessage) {
        StringBuilder context = new StringBuilder("Conversation so far:\n");

        for (Message msg : history) {
            context.append(msg.getSender()).append(": ")
                    .append(msg.getText()).append("\n");
        }

        context.append("scammer: ").append(newMessage).append("\n\n");
        context.append("Respond as the user:");

        return context.toString();
    }

    private String generateFallbackResponse(String scammerMessage, boolean scamDetected) {
        String lower = scammerMessage.toLowerCase();

        if (!scamDetected) {
            return "I'm not sure I understand. Could you clarify?";
        }

        if (lower.contains("account") || lower.contains("blocked")) {
            return "Oh no! What do I need to do? Can you help me fix this?";
        } else if (lower.contains("verify") || lower.contains("confirm")) {
            return "I'm worried. What information do you need from me?";
        } else if (lower.contains("click") || lower.contains("link")) {
            return "I'm not good with technology. Can you send me the link again?";
        } else if (lower.contains("upi") || lower.contains("payment")) {
            return "I want to help. What's your UPI ID so I can send it?";
        } else {
            return "I'm confused. Could you explain this more clearly? What exactly should I do?";
        }
    }

    public String selectPersona(String sessionId) {
        Random random = new Random(sessionId.hashCode());
        return PERSONAS.get(random.nextInt(PERSONAS.size()));
    }
}