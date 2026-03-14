package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormatWeekTotalsDTO {
    private Map<String, Map<Long, Integer>> byDayAndTurn;
    private Map<String, Integer> byDay;
    private Integer weekTotal;
}
