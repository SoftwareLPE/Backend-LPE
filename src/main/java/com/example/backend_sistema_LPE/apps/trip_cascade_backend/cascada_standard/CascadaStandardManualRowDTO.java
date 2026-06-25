package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import lombok.Data;

@Data
public class CascadaStandardManualRowDTO {
    private Long manualStandardRowId;
    private String driverNameOverride;
    private Long routeId;
    private String routeName;
    private Integer sortOrder;
}
