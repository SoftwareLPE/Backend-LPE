package com.example.backend_sistema_LPE.dto;

import lombok.Data;

@Data
public class UpdateCascadaStandardManualRowRequestDTO {
    private String driverNameOverride;
    private Long routeId;
    private String routeName;
    private Integer sortOrder;
}
