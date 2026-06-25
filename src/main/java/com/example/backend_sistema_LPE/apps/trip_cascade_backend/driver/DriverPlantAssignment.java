package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.DriverType;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.route.Route;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "driver_plant_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"driver_id", "plant_id"})
)
public class DriverPlantAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverPlantAssignmentId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Enumerated(EnumType.STRING)
    private DriverType driverType;
}
