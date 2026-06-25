package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.CascadaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CascadaStandardWeekRepository extends JpaRepository<CascadaStandardWeek, Long> {
    List<CascadaStandardWeek> findByStatus(CascadaStatus status);

    Optional<CascadaStandardWeek> findByPlantPlantIdAndWeekStartDateAndShiftId(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId
    );

    List<CascadaStandardWeek> findByPlantPlantIdAndWeekStartDateAndStatus(
            Long plantId,
            LocalDate weekStartDate,
            CascadaStatus status
    );
}
