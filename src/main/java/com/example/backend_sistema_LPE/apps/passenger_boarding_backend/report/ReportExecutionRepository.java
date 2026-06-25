package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.enums.ReportExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {

    Optional<ReportExecution> findTopByRequestKeyAndStatusOrderByExecutedAtDesc(
            String requestKey,
            ReportExecutionStatus status
    );

    Optional<ReportExecution> findTopByRequestKeyAndStatusInOrderByExecutedAtDesc(
            String requestKey,
            Collection<ReportExecutionStatus> statuses
    );
}
