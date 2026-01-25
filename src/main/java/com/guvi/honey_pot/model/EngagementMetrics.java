package com.guvi.honey_pot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngagementMetrics {
    private Integer engagementDurationSeconds;
    private Integer totalMessagesExchanged;
}
