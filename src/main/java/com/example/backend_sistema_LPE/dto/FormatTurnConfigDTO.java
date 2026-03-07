package com.example.backend_sistema_LPE.dto;

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
    private String dayOfWeek;
    private String turnName;
    private Integer sortOrder;
}
