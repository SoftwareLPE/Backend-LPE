package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapDTO;
import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapSaveRequestDTO;

import java.util.List;

public interface ShiftFormatTurnMapService {
    List<ShiftFormatTurnMapDTO> getMappings(Long plantId, Long formatTypeId);

    void saveMappings(ShiftFormatTurnMapSaveRequestDTO request);
}
