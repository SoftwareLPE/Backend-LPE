package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "regal_week_summary")
public class RegalWeekSummarySnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "regal_week_summary_id")
    private Long regalWeekSummaryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @Column(name = "week_date", nullable = false)
    private LocalDate weekDate;

    @Column(name = "normal_short", nullable = false)
    private Integer normalShort = 0;

    @Column(name = "normal_long", nullable = false)
    private Integer normalLong = 0;

    @Column(name = "extra_short", nullable = false)
    private Integer extraShort = 0;

    @Column(name = "extra_long", nullable = false)
    private Integer extraLong = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;
}
