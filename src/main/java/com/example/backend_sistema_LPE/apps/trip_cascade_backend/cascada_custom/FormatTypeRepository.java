package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormatTypeRepository extends JpaRepository<FormatType, Long> {
    Optional<FormatType> findByNameIgnoreCase(String name);

    Optional<FormatType> findByFormatCatalogId(Long formatCatalogId);
}
