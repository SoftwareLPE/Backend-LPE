package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.CascadaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlexsurWeekRepository extends JpaRepository<FlexsurWeek, Long> {
    List<FlexsurWeek> findByStatus(CascadaStatus status);
    List<FlexsurWeek> findByStatusAndPlantPlantId(CascadaStatus status, Long plantId);

    List<FlexsurWeek> findByPlantPlantIdAndWeekDate(Long plantId, LocalDate weekDate);

    List<FlexsurWeek> findByPlantPlantIdAndWeekDateAndShiftShiftId(
            Long plantId,
            LocalDate weekDate,
            Long shiftId
    );

    List<FlexsurWeek> findByManualRowManualFlexsurRowIdIn(List<Long> manualRowIds);

    void deleteByPlantPlantIdAndWeekDate(Long plantId, LocalDate weekDate);

    void deleteByPlantPlantIdAndWeekDateAndShiftShiftId(Long plantId, LocalDate weekDate, Long shiftId);
}
