package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.DriverType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverDTO {
    private String driverName;
    private String lastName;
    private Boolean active;
    private DriverType driverType;
    private Long routeId;
    private String routeName;
    private String routeLocation;
    private String unitType;
    private java.util.Set<Long> shiftIds;
    private Long plantId;

}
