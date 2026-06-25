package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.sql.Timestamp;

@Entity
@Table(name = "units", uniqueConstraints = {
                @UniqueConstraint(name = "uk_unit_plant_wialon", columnNames = {"plant_id", "wialon_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long unitId;

    @Column(name = "wialon_id", nullable = false)
    private Long wialonId;

    @Column(name = "name_raw", nullable = false, length = 255)
    private String nameRaw;

    @Column(length = 50)
    private String routeCode;

    @Column(length = 150)
    private String routeName;

    @Column(length = 50)
    private String internalId;

    @Column(nullable = false)
    private boolean isActive = true;

    private Timestamp lastSyncedAt;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore
    private Plant plant;
}
