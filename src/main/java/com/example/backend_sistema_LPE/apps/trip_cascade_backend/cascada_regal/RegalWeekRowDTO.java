package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalWeekRowDTO {
    private Long regalWeekId;
    private Long manualRegalRowId;
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private String driverType;
    private Long routeId;
    private String routeName;
    private String recorrido;
    private List<RegalDetailDTO> details;
    private Map<Long, Integer> totalsByTripType;
    private Integer rowTotal;
}
