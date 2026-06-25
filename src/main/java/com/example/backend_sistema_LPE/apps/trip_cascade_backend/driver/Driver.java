package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.shift.Shift;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driverId;
    private String driverName;
    private String lastName;
    private String email;
    private Boolean active;


    @OneToMany(mappedBy = "driver", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<DriverRoute> driverRoutes;


    @ManyToOne
    @JoinColumn(name = "plant_id")
    @JsonIgnore
    private Plant plant;

    @ManyToMany
    @JoinTable(
            name = "driver_shifts",
            joinColumns = @JoinColumn(name = "driver_id"),
            inverseJoinColumns = @JoinColumn(name = "shift_id")
    )
    @JsonIgnore
    private Set<Shift> shifts = new HashSet<>();

    @OneToMany(mappedBy = "driver", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private Set<DriverPlantAssignment> plantAssignments = new HashSet<>();
}
