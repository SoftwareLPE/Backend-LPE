package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalWeekSchemaTripTypeDTO {
    private Long tripTypeId;
    private String code;
    private String label;
    private Integer sortOrder;
    private List<String> dayKeys;
    private String totalColumn;
}
