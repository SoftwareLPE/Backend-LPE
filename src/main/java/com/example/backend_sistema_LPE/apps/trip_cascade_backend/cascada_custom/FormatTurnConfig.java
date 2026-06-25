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
        name = "format_turn_config",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_format_turn_config_format_day_turn",
                columnNames = {"format_type_id", "day_of_week", "turn_name"}
        )
)
public class FormatTurnConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "turn_config_id")
    private Long turnConfigId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "format_type_id", nullable = false)
    private FormatType formatType;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "turn_name", nullable = false)
    private String turnName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
