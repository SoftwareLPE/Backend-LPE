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
        name = "format_type_te_rule",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_format_type_te_rule_format_day",
                columnNames = {"format_type_id", "day_of_week"}
        )
)
public class FormatTypeTeRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "format_type_te_rule_id")
    private Long formatTypeTeRuleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "format_type_id", nullable = false)
    private FormatType formatType;

    @Column(name = "day_of_week", nullable = false)
    private String dayOfWeek;

    @Column(name = "te_count", nullable = false)
    private Integer teCount;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
