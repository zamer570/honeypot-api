package com.guvi.honey_pot.component;


import com.guvi.honey_pot.model.ExtractedIntelligence;
import com.guvi.honey_pot.model.SessionData;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public SessionData getOrCreateSession(String sessionId) {
        return sessions.computeIfAbsent(sessionId, k -> SessionData.builder()
                .sessionId(sessionId)
                .startTime(Instant.now())
                .scamDetected(false)
                .messageCount(0)
                .extractedIntelligence(ExtractedIntelligence.builder()
                        .bankAccounts(new ArrayList<>())
                        .upiIds(new ArrayList<>())
                        .phishingLinks(new ArrayList<>())
                        .phoneNumbers(new ArrayList<>())
                        .suspiciousKeywords(new ArrayList<>())
                        .build())
                .agentNotes(new StringBuilder())
                .build());
    }

    public void updateSession(String sessionId, SessionData data) {
        sessions.put(sessionId, data);
    }

    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }
}