package com.guvi.honey_pot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomingMessageRequest {
    private String sessionId;
    private Message message;
    private List<Message> conversationHistory;
    private Metadata metadata;
}
