package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "regal_trip_type",
        uniqueConstraints = @UniqueConstraint(columnNames = {"plant_id", "code"})
)
public class RegalTripType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_type_id")
    private Long tripTypeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @ElementCollection
    @CollectionTable(
            name = "regal_trip_type_days",
            joinColumns = @JoinColumn(name = "trip_type_id")
    )
    @Column(name = "day_key", nullable = false)
    private Set<String> dayKeys = new HashSet<>();
}
