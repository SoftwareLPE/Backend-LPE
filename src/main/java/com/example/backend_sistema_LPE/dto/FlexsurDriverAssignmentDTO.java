package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlexsurDriverAssignmentDTO {
    private Long assignmentId;
    private Long plantId;
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private String driverType;
    private Long serviceId;
    private String serviceName;
    private Long shiftId;
    private String shiftName;
    private Long routeId;
    private String routeName;
    private String routeLocation;
    private Set<String> dayKeys;
    private Boolean active;
}
