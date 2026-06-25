package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormatWeekTotalsDTO {
    private Map<String, Map<Long, Integer>> byDayAndTurn;
    private Map<String, Integer> byDay;
    private Integer weekTotal;
    private List<FormatWeekUnitTypeSummaryDTO> unitTypeSummary;
}
