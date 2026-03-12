package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CascadaRowDTO {
    private Long manualStandardRowId;
    private Long driverId;
    private String driverName;
    private String lastName;
    private Long routeId;
    private String routeName;

    @JsonProperty("E")
    private String e;

    @JsonProperty("S")
    private String s;

    @JsonProperty("ETE")
    private String ete;

    @JsonProperty("STE")
    private String ste;

    @JsonProperty("driverNameOverride")
    private String driverNameOverride;
}
