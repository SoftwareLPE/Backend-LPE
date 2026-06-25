package com.example.backend_sistema_LPE.apps.shared.shift;

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
        name = "shift_wialon_aliases",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"shift_id", "normalized_alias_name"})
        }
)
public class ShiftWialonAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shift_wialon_alias_id")
    private Long shiftWialonAliasId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "alias_name", nullable = false, length = 150)
    private String aliasName;

    @Column(name = "normalized_alias_name", nullable = false, length = 150)
    private String normalizedAliasName;
}
