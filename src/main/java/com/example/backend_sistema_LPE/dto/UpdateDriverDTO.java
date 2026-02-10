package com.example.backend_sistema_LPE.dto;

import com.example.backend_sistema_LPE.enums.DriverType;
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
    private java.util.Set<Long> shiftIds;
    private Long plantId;

}
