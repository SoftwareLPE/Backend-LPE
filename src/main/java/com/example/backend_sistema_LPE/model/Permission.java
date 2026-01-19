package com.example.backend_sistema_LPE.model;

import jakarta.persistence.Entity;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "permissions", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long permissionId;

    @Column(nullable = false, unique = true)
    private String code; // ej: "USER_CREATE", "USER_EDIT", etc.

    private String description;
}
