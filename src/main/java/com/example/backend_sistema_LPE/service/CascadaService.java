package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;

import java.time.LocalDate;

public interface CascadaService {
    CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey);

    void saveCascada(CascadaSaveRequestDTO request);
}
