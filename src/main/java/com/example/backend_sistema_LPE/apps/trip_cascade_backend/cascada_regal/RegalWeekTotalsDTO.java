package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegalWeekTotalsDTO {
    private Map<Long, Map<String, Integer>> byTripTypeDay;
    private Map<Long, Integer> byTripType;
    private Integer weekTotal;
    private RegalWeeklySummaryDTO weeklySummary;
}
