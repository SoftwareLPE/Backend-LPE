package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatWeekSchemaDTO {
    private Long formatTypeId;
    private List<String> baseColumns;
    private Map<String, List<FormatWeekTurnDTO>> days;
    private List<FormatExtraRowDTO> extraRows;
}
