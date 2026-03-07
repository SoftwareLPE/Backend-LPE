package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;

import java.time.LocalDate;

public interface RegalWeekService {
    RegalWeekResponseDTO getRegalWeek(Long plantId, LocalDate weekDate, Long shiftId);

    void saveRegalWeek(RegalWeekSaveRequestDTO request, Long userId);

    void updateRegalStatus(Long plantId, LocalDate weekDate, String status, Long userId);

    java.util.List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getRegalSummaries(
            String status,
            Long plantId,
            LocalDate weekDate
    );
}
