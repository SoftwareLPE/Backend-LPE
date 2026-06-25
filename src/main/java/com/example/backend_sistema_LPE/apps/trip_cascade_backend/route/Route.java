package com.example.backend_sistema_LPE.apps.trip_cascade_backend.route;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverRoute;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "routes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plant_id", "route_name"})
)
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long routeId;
    private String routeName;
    private String location;
    private String unitType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore
    private Plant plant;

    //Un recorrido puede tener varios driverRecorrido
    @OneToMany(mappedBy = "route", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<DriverRoute> driverRoutes;
}
