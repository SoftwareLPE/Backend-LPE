package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FlexsurManualRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlexsurManualRowRepository extends JpaRepository<FlexsurManualRow, Long> {
    List<FlexsurManualRow> findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualFlexsurRowIdAsc(
            Long plantId,
            LocalDate weekDate
    );

    List<FlexsurManualRow> findByPlantPlantIdAndWeekDateAndShiftShiftIdOrderBySortOrderAscManualFlexsurRowIdAsc(
            Long plantId,
            LocalDate weekDate,
            Long shiftId
    );

    List<FlexsurManualRow> findByPlantPlantIdAndWeekDateAndShiftIsNullOrderBySortOrderAscManualFlexsurRowIdAsc(
            Long plantId,
            LocalDate weekDate
    );

    void deleteByPlantCompanyCompanyId(Long companyId);
}
