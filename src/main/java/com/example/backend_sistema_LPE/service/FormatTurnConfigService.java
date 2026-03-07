package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatTurnConfigDTO;

import java.util.List;

public interface FormatTurnConfigService {
    List<FormatTurnConfigDTO> getByFormatType(Long formatTypeId);
}
