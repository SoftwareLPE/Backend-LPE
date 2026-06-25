package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatWeekRowDTO {
    private Long formatWeekId;
    private Long manualRowId;
    private Long routeId;
    private String routeName;
    private Long driverId;
    private String driverName;
    private String driverLastName;
    private String unitType;
    private String secondaryValue;
    private Boolean extraRow;
    private List<FormatWeekCellDTO> cells;
    private Integer rowTotal;
}
