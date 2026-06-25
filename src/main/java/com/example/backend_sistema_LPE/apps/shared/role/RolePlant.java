package com.example.backend_sistema_LPE.apps.shared.role;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import jakarta.persistence.Entity;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "role_plants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "plant_id"})
)
public class RolePlant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rolePlantId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    private LocalDateTime createdAt;
}
