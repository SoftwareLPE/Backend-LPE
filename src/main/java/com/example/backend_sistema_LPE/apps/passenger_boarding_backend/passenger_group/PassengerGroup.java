package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger_group;

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
@Table(
        name = "passenger_groups",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_passenger_group_plant_wialon", columnNames = {"plant_id", "wialon_id"})
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PassengerGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerGroupId;

    @Column(name = "wialon_id", nullable = false)
    private Long wialonId;

    @Column(nullable = false, length = 200)
    private String name;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    @JsonIgnore
    private Plant plant;

    @Column(nullable = false)
    private boolean isDefault;

    @Column(nullable = false)
    private boolean isActive = true;

    private Timestamp lastSyncedAt;
}
