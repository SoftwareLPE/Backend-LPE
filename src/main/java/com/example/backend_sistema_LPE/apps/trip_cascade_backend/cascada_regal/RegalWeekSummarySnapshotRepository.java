package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RegalWeekSummarySnapshotRepository extends JpaRepository<RegalWeekSummarySnapshot, Long> {
    Optional<RegalWeekSummarySnapshot> findByPlantPlantIdAndWeekDate(Long plantId, LocalDate weekDate);
}
