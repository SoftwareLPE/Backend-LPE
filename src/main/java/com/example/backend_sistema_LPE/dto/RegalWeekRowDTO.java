package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalWeekRowDTO {
    private Long regalWeekId;
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private Long routeId;
    private String routeName;
    private String recorrido;
    private List<RegalDetailDTO> details;
}
