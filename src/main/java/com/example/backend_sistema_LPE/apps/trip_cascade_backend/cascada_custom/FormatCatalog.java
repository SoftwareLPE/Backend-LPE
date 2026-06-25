package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "format_catalog")
public class FormatCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "format_catalog_id")
    private Long formatCatalogId;

    @Column(name = "format_code", length = 50, nullable = false, unique = true)
    private String formatCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "format_category", nullable = false)
    private String formatCategory;

    @Column(name = "active", nullable = false)
    private Boolean active = Boolean.TRUE;
}
