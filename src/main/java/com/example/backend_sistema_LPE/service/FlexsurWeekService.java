package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FlexsurWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSaveRequestDTO;

import java.time.LocalDate;

public interface FlexsurWeekService {
    FlexsurWeekResponseDTO getFlexsurWeek(Long plantId, LocalDate weekDate);

    void saveFlexsurWeek(FlexsurWeekSaveRequestDTO request, Long userId);

    void updateFlexsurStatus(Long plantId, LocalDate weekDate, String status, Long userId);

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getFlexsurSummaries(
            String status,
            Long plantId,
            LocalDate weekDate
    );
}
