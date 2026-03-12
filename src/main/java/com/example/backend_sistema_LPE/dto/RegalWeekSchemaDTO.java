package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalWeekSchemaDTO {
    private Long plantId;
    private List<String> baseColumns;
    private List<RegalWeekSchemaTripTypeDTO> tripTypes;
    private String totalColumn;
}
