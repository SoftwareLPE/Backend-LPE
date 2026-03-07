package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FormatCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FormatCatalogRepository extends JpaRepository<FormatCatalog, Long> {
    List<FormatCatalog> findByActiveTrue();
}
