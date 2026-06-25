package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "format_week_cell")
public class FormatWeekCell {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cell_id")
    private Long cellId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "format_week_id", nullable = false)
    private FormatWeek formatWeek;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turn_config_id", nullable = false)
    private FormatTurnConfig turnConfig;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "trip_count", nullable = false)
    private Integer tripCount = 0;
}
