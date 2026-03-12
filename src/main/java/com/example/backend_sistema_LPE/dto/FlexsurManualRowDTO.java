package com.example.backend_sistema_LPE.dto;

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
    private String serviceName;
    private Integer sortOrder;
}
