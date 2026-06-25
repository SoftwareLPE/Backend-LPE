package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FormatWeekTotalsSnapshotRepository extends JpaRepository<FormatWeekTotalsSnapshot, Long> {
    Optional<FormatWeekTotalsSnapshot> findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateAndShiftShiftId(
            Long plantId,
            Long formatTypeId,
            LocalDate weekDate,
            Long shiftId
    );

    Optional<FormatWeekTotalsSnapshot> findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateAndShiftIsNull(
            Long plantId,
            Long formatTypeId,
            LocalDate weekDate
    );
}
