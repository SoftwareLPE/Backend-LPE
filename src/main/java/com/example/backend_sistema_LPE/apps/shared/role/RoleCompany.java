package com.example.backend_sistema_LPE.apps.shared.role;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.Company;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "role_companies",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "company_id"})
)
public class RoleCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleCompanyId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private LocalDateTime createdAt;
}
