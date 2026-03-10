package com.example.backend_sistema_LPE.model;

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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "format_week_manual_row")
public class FormatWeekManualRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manual_row_id")
    private Long manualRowId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plant_id", nullable = false)
    private Plant plant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "format_type_id", nullable = false)
    private FormatType formatType;

    @Column(name = "week_date", nullable = false)
    private LocalDate weekDate;

    @Column(name = "route_name")
    private String routeName;

    @Column(name = "driver_name")
    private String driverName;

    @Column(name = "driver_last_name")
    private String driverLastName;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "secondary_value")
    private String secondaryValue;

    @Column(name = "extra_row", nullable = false)
    private Boolean extraRow = Boolean.TRUE;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by_user_id")
    private Long updatedByUserId;
}
