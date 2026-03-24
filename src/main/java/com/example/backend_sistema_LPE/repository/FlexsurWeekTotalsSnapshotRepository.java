package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FlexsurWeekTotalsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FlexsurWeekTotalsSnapshotRepository extends JpaRepository<FlexsurWeekTotalsSnapshot, Long> {
    Optional<FlexsurWeekTotalsSnapshot> findByPlantPlantIdAndWeekDateAndShiftShiftId(Long plantId, LocalDate weekDate, Long shiftId);
    Optional<FlexsurWeekTotalsSnapshot> findByPlantPlantIdAndWeekDateAndShiftIsNull(Long plantId, LocalDate weekDate);
}
