package com.guvi.honey_pot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionData {
    private String sessionId;
    private Instant startTime;
    private Boolean scamDetected;
    private Integer messageCount;
    private ExtractedIntelligence extractedIntelligence;
    private StringBuilder agentNotes;
    private String currentPersona;
}