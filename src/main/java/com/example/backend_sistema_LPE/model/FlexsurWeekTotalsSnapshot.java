package com.example.backend_sistema_LPE.model;

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
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flexsur_week_totals")
public class FlexsurWeekTotalsSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flexsur_week_totals_id")
    private Long flexsurWeekTotalsId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "week_date", nullable = false)
    private LocalDate weekDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @ElementCollection
    @CollectionTable(
            name = "flexsur_week_totals_by_day",
            joinColumns = @JoinColumn(name = "flexsur_week_totals_id")
    )
    @MapKeyColumn(name = "service_date", nullable = false)
    @Column(name = "day_total", nullable = false)
    private Map<LocalDate, Integer> byDay = new LinkedHashMap<>();

    @Column(name = "week_total", nullable = false)
    private Integer weekTotal = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;
}
