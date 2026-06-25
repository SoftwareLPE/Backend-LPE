package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateFlexsurDriverAssignmentRequestDTO {
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private String driverType;
    private Long serviceId;
    private Long shiftId;
    private Long routeId;
    @JsonAlias("recorrido")
    private String routeLocation;
    private Set<String> dayKeys;
    private Boolean active;
}
