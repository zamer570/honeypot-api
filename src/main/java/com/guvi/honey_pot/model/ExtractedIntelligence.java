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
public class ExtractedIntelligence {
    private List<String> bankAccounts;
    private List<String> upiIds;
    private List<String> phishingLinks;
    private List<String> phoneNumbers;
    private List<String> suspiciousKeywords;
}