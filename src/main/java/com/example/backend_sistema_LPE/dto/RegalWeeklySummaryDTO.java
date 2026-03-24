package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegalWeeklySummaryDTO {
    private Integer normalShort;
    private Integer normalLong;
    private Integer extraShort;
    private Integer extraLong;
}
