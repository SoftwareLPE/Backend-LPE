package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateDriverShiftsDTO {
    private Set<Long> shiftIds;
}
