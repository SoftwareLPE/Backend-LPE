package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UnitSummaryDTO {
    @JsonProperty("unit_id")
    private Long unitId;

    @JsonProperty("unit_name")
    private String unitName;

    @JsonProperty("internal_id")
    private String internalId;

    @JsonProperty("route_code")
    private String routeCode;

    @JsonProperty("route_name")
    private String routeName;

    @JsonProperty("wialon_id")
    private Long wialonId;
}
