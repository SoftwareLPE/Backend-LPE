package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalManualRowDTO {
    private Long manualRegalRowId;
    private String driverNameOverride;
    private String routeName;
    private String routeLocation;
    private Integer sortOrder;
}
