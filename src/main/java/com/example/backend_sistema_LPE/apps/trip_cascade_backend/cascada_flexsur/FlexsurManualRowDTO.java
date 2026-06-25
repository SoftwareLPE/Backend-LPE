package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlexsurManualRowDTO {
    private Long manualFlexsurRowId;
    private Long shiftId;
    private String serviceName;
    private Integer sortOrder;
}
