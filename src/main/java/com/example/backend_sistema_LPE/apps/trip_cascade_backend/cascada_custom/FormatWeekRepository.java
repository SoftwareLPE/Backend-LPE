package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.CascadaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FormatWeekRepository extends JpaRepository<FormatWeek, Long> {
    List<FormatWeek> findByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
            Long plantId,
            LocalDate weekDate,
            Long formatTypeId
    );

    List<FormatWeek> findByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
            Long plantId,
            LocalDate weekDate,
            Long shiftId,
            Long formatTypeId
    );

    void deleteByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
            Long plantId,
            LocalDate weekDate,
            Long formatTypeId
    );

    void deleteByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
            Long plantId,
            LocalDate weekDate,
            Long shiftId,
            Long formatTypeId
    );

    List<FormatWeek> findByStatus(CascadaStatus status);

    List<FormatWeek> findByStatusAndPlantPlantId(
            CascadaStatus status,
            Long plantId
    );

    List<FormatWeek> findByStatusAndWeekDate(
            CascadaStatus status,
            LocalDate weekDate
    );

    List<FormatWeek> findByStatusAndPlantPlantIdAndWeekDate(
            CascadaStatus status,
            Long plantId,
            LocalDate weekDate
    );

    List<FormatWeek> findByManualRowManualRowIdIn(List<Long> manualRowIds);
}
