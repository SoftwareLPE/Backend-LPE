package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FormatWeek;
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

    List<FormatWeek> findByStatus(com.example.backend_sistema_LPE.enums.CascadaStatus status);

    List<FormatWeek> findByStatusAndPlantPlantId(
            com.example.backend_sistema_LPE.enums.CascadaStatus status,
            Long plantId
    );

    List<FormatWeek> findByStatusAndWeekDate(
            com.example.backend_sistema_LPE.enums.CascadaStatus status,
            LocalDate weekDate
    );

    List<FormatWeek> findByStatusAndPlantPlantIdAndWeekDate(
            com.example.backend_sistema_LPE.enums.CascadaStatus status,
            Long plantId,
            LocalDate weekDate
    );
}
