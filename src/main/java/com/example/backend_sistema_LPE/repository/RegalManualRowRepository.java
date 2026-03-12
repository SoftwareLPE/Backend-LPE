package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.RegalManualRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegalManualRowRepository extends JpaRepository<RegalManualRow, Long> {
    List<RegalManualRow> findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualRegalRowIdAsc(
            Long plantId,
            LocalDate weekDate
    );

    void deleteByPlantCompanyCompanyId(Long companyId);
}
