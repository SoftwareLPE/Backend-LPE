package com.example.backend_sistema_LPE.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
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


    @OneToMany(mappedBy = "driver")
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

    @OneToMany(mappedBy = "driver")
    @JsonIgnore
    private Set<DriverPlantAssignment> plantAssignments = new HashSet<>();
}
