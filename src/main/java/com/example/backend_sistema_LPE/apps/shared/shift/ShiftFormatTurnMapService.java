package com.example.backend_sistema_LPE.apps.shared.shift;

import java.util.List;

public interface ShiftFormatTurnMapService {
    List<ShiftFormatTurnMapDTO> getMappings(Long plantId, Long formatTypeId);

    void saveMappings(ShiftFormatTurnMapSaveRequestDTO request);
}
