package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormatTurnConfigRepository extends JpaRepository<FormatTurnConfig, Long> {
    List<FormatTurnConfig> findByFormatTypeFormatTypeId(Long formatTypeId);

    List<FormatTurnConfig> findByFormatTypeFormatTypeIdAndDayOfWeek(Long formatTypeId, String dayOfWeek);

    Optional<FormatTurnConfig> findByFormatTypeFormatTypeIdAndDayOfWeekAndTurnName(
            Long formatTypeId,
            String dayOfWeek,
            String turnName
    );

    void deleteByFormatTypeFormatTypeIdAndDayOfWeekAndTurnName(
            Long formatTypeId,
            String dayOfWeek,
            String turnName
    );
}
