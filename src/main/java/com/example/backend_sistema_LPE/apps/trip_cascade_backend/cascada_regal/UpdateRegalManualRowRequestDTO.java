package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRegalManualRowRequestDTO {
    private String driverNameOverride;
    private String routeName;
    @JsonAlias("recorrido")
    private String routeLocation;
    private Integer sortOrder;
}
