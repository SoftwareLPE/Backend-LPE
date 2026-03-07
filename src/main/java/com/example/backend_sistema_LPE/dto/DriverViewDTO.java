package com.example.backend_sistema_LPE.dto;

import com.example.backend_sistema_LPE.enums.DriverType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DriverViewDTO {
    private Long driverId;
    private String driverName;
    private String lastName;
    private Boolean active;
    private java.util.Set<Long> shiftIds;

//    private Long routeId;
    private String routeName;
    private String routeLocation;
    private String unitType;

    private DriverType driverType;
//    private String shift;
//    private LocalDate assignmentDate;
//    private String notes;
}
