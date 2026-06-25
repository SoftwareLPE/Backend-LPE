package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "format_type")
public class FormatType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "format_type_id")
    private Long formatTypeId;

    @Column(name = "format_catalog_id")
    private Long formatCatalogId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "secondary_column")
    private String secondaryColumn;

    @Column(name = "includes_unit_type", nullable = false)
    private Boolean includesUnitType = false;
}
