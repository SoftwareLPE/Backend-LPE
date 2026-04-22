package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FlexsurWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FlexsurWeekRepository extends JpaRepository<FlexsurWeek, Long> {
    List<FlexsurWeek> findByStatus(com.example.backend_sistema_LPE.enums.CascadaStatus status);
    List<FlexsurWeek> findByStatusAndPlantPlantId(com.example.backend_sistema_LPE.enums.CascadaStatus status, Long plantId);

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
