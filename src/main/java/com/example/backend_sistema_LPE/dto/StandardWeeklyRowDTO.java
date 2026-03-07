package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StandardWeeklyRowDTO {
    private Long driverId;
    private String driverName;
    private String lastName;
    private String driverNameOverride;
    private Long routeId;
    private String routeName;
    private String e;
    private String s;
    private String ete;
    private String ste;
    private Integer total;
}
