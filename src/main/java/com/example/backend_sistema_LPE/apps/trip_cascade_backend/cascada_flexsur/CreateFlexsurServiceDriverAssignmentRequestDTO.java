package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateFlexsurServiceDriverAssignmentRequestDTO {
    private Long plantId;
    private Long serviceId;
    private Long driverId;
    private Boolean active;
}
