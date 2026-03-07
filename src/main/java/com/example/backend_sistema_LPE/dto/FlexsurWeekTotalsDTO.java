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
public class FlexsurWeekTotalsDTO {
    private Map<String, Integer> byDay;
    private Map<String, Integer> byService;
    private Map<String, Integer> byColumn;
    private Integer weekTotal;
}
