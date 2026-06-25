package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.DriverType;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.route.Route;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "drivers_routes")
public class DriverRoute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverRouteId;
    private Date assigmentDate;
    private String shift;

    @Enumerated(EnumType.STRING)
    private DriverType driverType;

    private String notes;

    //Un driver_recorrido  lo pueden hacer distintos choferes
    @ManyToOne
    @JoinColumn(name = "driver_id",nullable = true)
    private Driver driver;

    //Un chofer puede hacer varios recorridos
    @ManyToOne
    @JoinColumn(name = "route_id",nullable = true)
    private Route route;

}
