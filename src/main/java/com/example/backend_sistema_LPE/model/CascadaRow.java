package com.example.backend_sistema_LPE.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "cascada_rows",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"plant_id", "week_start_date", "turno_id", "day_key", "driver_id"}
        )
)
public class CascadaRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cascadaRowId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    @Column(name = "shift_id", nullable = false)
    private String shiftId;

    @Column(name = "day_key", nullable = false)
    private String dayKey;

    @Column(name = "e_value")
    private String eValue;

    @Column(name = "s_value")
    private String sValue;

    @Column(name = "ete_value")
    private String eteValue;

    @Column(name = "ste_value")
    private String steValue;
}
