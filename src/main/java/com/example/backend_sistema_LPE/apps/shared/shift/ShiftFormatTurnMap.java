package com.example.backend_sistema_LPE.apps.shared.shift;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatType;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
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

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "shift_format_turn_map",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"plant_id", "format_type_id", "shift_id", "day_of_week"}
                )
        }
)
public class ShiftFormatTurnMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "map_id")
    private Long mapId;

    @ManyToOne
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne
    @JoinColumn(name = "format_type_id", nullable = false)
    private FormatType formatType;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "turn_name", nullable = false)
    private String turnName;
}
