package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateShiftRequestDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateShiftRequestDTO;

import java.util.List;

public interface ShiftService {
    List<ShiftDTO> getShiftsByPlant(Long plantId);

    ShiftDTO createShift(Long plantId, CreateShiftRequestDTO request);

    ShiftDTO updateShift(Long plantId, Long shiftId, UpdateShiftRequestDTO request);

    void deleteShift(Long plantId, Long shiftId);
}
