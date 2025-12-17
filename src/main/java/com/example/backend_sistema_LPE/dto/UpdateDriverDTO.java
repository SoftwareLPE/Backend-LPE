package com.example.backend_sistema_LPE.dto;

import com.example.backend_sistema_LPE.enums.DriverType;
import lombok.*;

@Data
public class UpdateDriverDTO {
    private final String driverName;
    private final String lastName;
    private final DriverType driverType;
    private final Long routeId;
    private final String routeName;

}
