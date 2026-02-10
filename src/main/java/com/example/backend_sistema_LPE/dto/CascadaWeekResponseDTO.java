package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CascadaWeekResponseDTO {
    private Long plantId;
    private LocalDate weekStartDate;
    private String status;
    private List<CascadaWeekItemDTO> items;
}
