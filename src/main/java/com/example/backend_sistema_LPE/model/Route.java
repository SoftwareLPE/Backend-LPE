package com.example.backend_sistema_LPE.model;

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
    private String routeType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore
    private Plant plant;

    //Un recorrido puede tener varios driverRecorrido
    @OneToMany(mappedBy = "route")
    @JsonIgnore
    private List<DriverRoute> driverRoutes;
}
