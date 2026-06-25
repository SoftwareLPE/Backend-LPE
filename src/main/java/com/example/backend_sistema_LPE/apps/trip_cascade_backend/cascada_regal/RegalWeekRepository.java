package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.CascadaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RegalWeekRepository extends JpaRepository<RegalWeek, Long> {
    List<RegalWeek> findByStatus(CascadaStatus status);
    List<RegalWeek> findByStatusAndPlantPlantId(CascadaStatus status, Long plantId);

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
