package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatTypeDTO {
    private Long formatTypeId;
    private String name;
    private String secondaryColumn;
    private Boolean includesUnitType;
}
