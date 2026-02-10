package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.CascadaRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CascadaRecipientRepository extends JpaRepository<CascadaRecipient, Long> {
    void deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey
    );

    void deleteByPlantPlantIdAndWeekStartDateAndShiftId(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId
    );

    @Query("""
        select distinct rc.recipientUserId
        from CascadaRecipient rc
        where rc.plant.plantId = :plantId
          and rc.weekStartDate = :weekStartDate
          and rc.shiftId = :shiftId
          and (:dayKey is null or rc.dayKey = :dayKey)
    """)
    List<Long> findRecipientUserIds(
            @Param("plantId") Long plantId,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("shiftId") String shiftId,
            @Param("dayKey") String dayKey
    );
}
