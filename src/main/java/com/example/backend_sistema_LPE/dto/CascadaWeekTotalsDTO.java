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
public class CascadaWeekTotalsDTO {
    private Map<Long, Integer> byDriver;
    private Map<Long, Integer> byRoute;
    private Map<String, Integer> byDay;
    private Map<String, Integer> byShift;
    private Map<String, Integer> byDriverType;
    private Map<String, Integer> byColumn;
    private Integer weekTotal;
}
