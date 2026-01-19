package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.CascadaRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CascadaRowRepository extends JpaRepository<CascadaRow, Long> {
    List<CascadaRow> findByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey
    );

    void deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey
    );

    void deleteByPlantPlantId(Long plantId);

    void deleteByPlantCompanyCompanyId(Long companyId);
}
