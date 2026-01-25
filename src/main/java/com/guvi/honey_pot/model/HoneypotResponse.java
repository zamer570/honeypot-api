package com.guvi.honey_pot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoneypotResponse {
    private String status;
    private Boolean scamDetected;
    private EngagementMetrics engagementMetrics;
    private ExtractedIntelligence extractedIntelligence;
    private String agentNotes;
    private Message agentResponse;
}