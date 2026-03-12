package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlexsurServiceDriverAssignmentDTO {
    private Long assignmentId;
    private Long plantId;
    private Long serviceId;
    private String serviceName;
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private Boolean active;
}
