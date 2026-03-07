package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FormatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormatTypeRepository extends JpaRepository<FormatType, Long> {
    Optional<FormatType> findByNameIgnoreCase(String name);
}
