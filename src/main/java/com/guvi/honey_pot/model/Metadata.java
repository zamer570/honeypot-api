package com.guvi.honey_pot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private String channel;
    private String language;
    private String locale;
}