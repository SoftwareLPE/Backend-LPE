package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DriverShiftDTO {
    private Long driverId;
    private String driverName;
    private String lastName;
    private String driverType;
    private List<Long> shiftIds;
    private Long routeId;
    private String routeName;
    private String routeLocation;
    private String unitType;
}
