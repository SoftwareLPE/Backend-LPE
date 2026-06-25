package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FormatTypeTeRuleRepository extends JpaRepository<FormatTypeTeRule, Long> {
    Optional<FormatTypeTeRule> findByFormatTypeFormatTypeIdAndDayOfWeek(
            Long formatTypeId,
            String dayOfWeek
    );

    Optional<FormatTypeTeRule> findByFormatTypeFormatTypeIdAndDayOfWeekAndActiveTrue(
            Long formatTypeId,
            String dayOfWeek
    );
}
