package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.RegalWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegalWeekRepository extends JpaRepository<RegalWeek, Long> {
    List<RegalWeek> findByStatus(com.example.backend_sistema_LPE.enums.CascadaStatus status);
    List<RegalWeek> findByStatusAndPlantPlantId(com.example.backend_sistema_LPE.enums.CascadaStatus status, Long plantId);

    List<RegalWeek> findByPlantPlantIdAndWeekDate(Long plantId, LocalDate weekDate);

    List<RegalWeek> findByPlantPlantIdAndWeekDateAndShiftShiftId(
            Long plantId,
            LocalDate weekDate,
            Long shiftId
    );

    List<RegalWeek> findByManualRowManualRegalRowIdIn(List<Long> manualRowIds);

    void deleteByPlantPlantIdAndWeekDate(Long plantId, LocalDate weekDate);

    void deleteByPlantPlantIdAndWeekDateAndShiftShiftId(Long plantId, LocalDate weekDate, Long shiftId);
}
