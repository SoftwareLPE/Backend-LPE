package com.example.backend_sistema_LPE.apps.shared.shift;

import java.util.List;

public interface ShiftService {
    List<ShiftDTO> getShiftsByPlant(Long plantId);

    ShiftDTO createShift(Long plantId, CreateShiftRequestDTO request);

    ShiftDTO updateShift(Long plantId, Long shiftId, UpdateShiftRequestDTO request);

    void deleteShift(Long plantId, Long shiftId);
}
