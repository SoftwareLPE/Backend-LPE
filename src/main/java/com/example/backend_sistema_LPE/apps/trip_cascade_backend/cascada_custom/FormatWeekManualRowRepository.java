package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FormatWeekManualRowRepository extends JpaRepository<FormatWeekManualRow, Long> {
    List<FormatWeekManualRow> findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateOrderBySortOrderAscManualRowIdAsc(
            Long plantId,
            Long formatTypeId,
            LocalDate weekDate
    );

    void deleteByPlantCompanyCompanyId(Long companyId);
}
