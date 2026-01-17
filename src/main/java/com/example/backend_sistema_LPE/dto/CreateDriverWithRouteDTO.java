package com.example.backend_sistema_LPE.dto;

import com.example.backend_sistema_LPE.enums.DriverType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateDriverWithRouteDTO {
    // Datos del chofer
    private String driverName;
    private String lastName;
    private Long plantId;
    private Boolean active;

    // Datos de la ruta
    private Long routeId;      // opcional
    private String routeName;  // opcional, según el caso

    // Datos de la asignación
    private DriverType driverType; // TITULAR / EXTRA
//    private String shift;
//    private String notes;
    //private String notes;
}
