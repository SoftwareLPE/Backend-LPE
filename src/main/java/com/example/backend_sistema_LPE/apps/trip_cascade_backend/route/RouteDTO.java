package com.example.backend_sistema_LPE.apps.trip_cascade_backend.route;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RouteDTO {
    private Long routeId;
    private String routeName;
    private String location;
}
