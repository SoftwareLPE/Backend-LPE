package com.example.backend_sistema_LPE.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
        name = "cascada_standard_cell",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cascada_standard_week_id", "day_key", "driver_id", "route_id"})
)
public class CascadaStandardCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cascadaStandardCellId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cascada_standard_week_id", nullable = false)
    private CascadaStandardWeek week;

    @Column(name = "day_key", nullable = false)
    private String dayKey;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(name = "route_id")
    private Long routeId;

    @Column(name = "val_e")
    private String e;

    @Column(name = "val_s")
    private String s;

    @Column(name = "val_ete")
    private String ete;

    @Column(name = "val_ste")
    private String ste;

    @Column(name = "driver_name_override")
    private String driverNameOverride;
}
