package com.guvi.honey_pot.controller;

import com.guvi.honey_pot.component.SessionManager;
import com.guvi.honey_pot.model.*;
import com.guvi.honey_pot.service.AIAgentService;
import com.guvi.honey_pot.service.CallbackService;
import com.guvi.honey_pot.service.ScamDetector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/honeypot")
public class HoneypotController {

//    @Value("${honeypot.api-key}")
    private String apiKey;

    private final SessionManager sessionManager;
    private final ScamDetector scamDetector;
    private final AIAgentService aiAgent;
    private final CallbackService callbackService;

    private static final int MAX_ENGAGEMENT_MESSAGES = 20;
    private static final int MIN_INTELLIGENCE_THRESHOLD = 2;

    public HoneypotController(SessionManager sessionManager,
                              ScamDetector scamDetector,
                              AIAgentService aiAgent,
                              CallbackService callbackService) {
        this.sessionManager = sessionManager;
        this.scamDetector = scamDetector;
        this.aiAgent = aiAgent;
        this.callbackService = callbackService;
    }

    @GetMapping
    public ResponseEntity<String> getHealth() {
        return ResponseEntity.ok("I am up!");
    }

    @PostMapping("/message")
    public ResponseEntity<?> handleMessage(
            @RequestHeader("x-api-key") String providedKey,
            @RequestBody IncomingMessageRequest request) {

        // Authenticate
        if (!apiKey.equals(providedKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid API key"));
        }

        // Get or create session
        SessionData session = sessionManager.getOrCreateSession(request.getSessionId());
        session.setMessageCount(session.getMessageCount() + 1);

        // Build conversation history
        List<String> historyTexts = new ArrayList<>();
        if (request.getConversationHistory() != null) {
            request.getConversationHistory().forEach(msg ->
                    historyTexts.add(msg.getText()));
        }

        String scammerText = request.getMessage().getText();

        // Detect scam intent
        boolean isScam = scamDetector.detectScamIntent(scammerText, historyTexts);

        if (isScam && !session.getScamDetected()) {
            session.setScamDetected(true);
            session.setCurrentPersona(aiAgent.selectPersona(request.getSessionId()));
            session.getAgentNotes().append("Scam detected. Engagement started. ");
        }

        // Extract intelligence
        ExtractedIntelligence intel = scamDetector.extractIntelligence(
                scammerText, session.getExtractedIntelligence());
        session.setExtractedIntelligence(intel);

        // Generate AI response
        String agentResponseText = aiAgent.generateResponse(
                scammerText,
                request.getConversationHistory() != null ?
                        request.getConversationHistory() : new ArrayList<>(),
                session.getCurrentPersona(),
                session.getScamDetected()
        );

        // Update session notes
        if (session.getScamDetected()) {
            updateAgentNotes(session, scammerText);
        }

        // Check if engagement should end
        boolean shouldEndEngagement = shouldEndEngagement(session, intel);

        // Calculate metrics
        long durationSeconds = Duration.between(
                session.getStartTime(), Instant.now()).getSeconds();

        // Build response
        HoneypotResponse response = HoneypotResponse.builder()
                .status("success")
                .scamDetected(session.getScamDetected())
                .engagementMetrics(EngagementMetrics.builder()
                        .engagementDurationSeconds((int) durationSeconds)
                        .totalMessagesExchanged(session.getMessageCount())
                        .build())
                .extractedIntelligence(intel)
                .agentNotes(session.getAgentNotes().toString())
                .agentResponse(Message.builder()
                        .sender("user")
                        .text(agentResponseText)
                        .timestamp(Instant.now().toString())
                        .build())
                .build();

        // Send final callback if engagement complete
        if (shouldEndEngagement && session.getScamDetected()) {
            sendFinalCallback(request.getSessionId(), session);
            sessionManager.removeSession(request.getSessionId());
        } else {
            sessionManager.updateSession(request.getSessionId(), session);
        }

        return ResponseEntity.ok(response);
    }

    private boolean shouldEndEngagement(SessionData session, ExtractedIntelligence intel) {
        // End if max messages reached
        if (session.getMessageCount() >= MAX_ENGAGEMENT_MESSAGES) {
            return true;
        }

        // End if sufficient intelligence gathered
        int intelCount = intel.getBankAccounts().size() +
                intel.getUpiIds().size() +
                intel.getPhishingLinks().size() +
                intel.getPhoneNumbers().size();

        return intelCount >= MIN_INTELLIGENCE_THRESHOLD;
    }

    private void updateAgentNotes(SessionData session, String scammerMessage) {
        String lower = scammerMessage.toLowerCase();

        if (lower.contains("urgent") || lower.contains("immediately")) {
            session.getAgentNotes().append("Urgency tactics detected. ");
        }
        if (lower.contains("blocked") || lower.contains("suspended")) {
            session.getAgentNotes().append("Account threat tactics. ");
        }
        if (lower.contains("verify") || lower.contains("confirm")) {
            session.getAgentNotes().append("Verification request. ");
        }
        if (lower.contains("payment") || lower.contains("transfer")) {
            session.getAgentNotes().append("Payment redirection attempt. ");
        }
    }

    private void sendFinalCallback(String sessionId, SessionData session) {
        FinalResultCallback callback = FinalResultCallback.builder()
                .sessionId(sessionId)
                .scamDetected(session.getScamDetected())
                .totalMessagesExchanged(session.getMessageCount())
                .extractedIntelligence(session.getExtractedIntelligence())
                .agentNotes(session.getAgentNotes().toString())
                .build();

        callbackService.sendFinalResult(callback);
    }
}
