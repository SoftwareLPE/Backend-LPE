package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatTurnConfigDTO {
    private Long turnConfigId;
    private Long formatTypeId;
    private Long shiftId;
    private String dayOfWeek;
    private String turnName;
    private Integer sortOrder;
}
