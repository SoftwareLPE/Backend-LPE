package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FormatWeekCell;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormatWeekCellRepository extends JpaRepository<FormatWeekCell, Long> {
}
