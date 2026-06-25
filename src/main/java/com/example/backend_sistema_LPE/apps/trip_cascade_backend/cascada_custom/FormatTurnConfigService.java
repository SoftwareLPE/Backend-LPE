package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import java.util.List;

public interface FormatTurnConfigService {
    List<FormatTurnConfigDTO> getByFormatType(Long formatTypeId, Long plantId);
}
