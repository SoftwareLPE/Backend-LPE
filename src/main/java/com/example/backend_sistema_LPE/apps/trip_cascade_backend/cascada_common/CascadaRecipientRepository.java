package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.CascadaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CascadaRecipientRepository extends JpaRepository<CascadaRecipient, Long> {
    void deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKeyAndCascadaType(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            CascadaType cascadaType
    );

    void deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndCascadaType(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            CascadaType cascadaType
    );

    @Query("""
        select distinct rc.recipientUserId
        from CascadaRecipient rc
        where rc.plant.plantId = :plantId
          and rc.weekStartDate = :weekStartDate
          and rc.shiftId = :shiftId
          and rc.cascadaType = :cascadaType
    """)
    List<Long> findRecipientUserIdsByShift(
            @Param("plantId") Long plantId,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("shiftId") String shiftId,
            @Param("cascadaType") CascadaType cascadaType
    );

    @Query("""
        select distinct rc.recipientUserId
        from CascadaRecipient rc
        where rc.plant.plantId = :plantId
          and rc.weekStartDate = :weekStartDate
          and rc.shiftId = :shiftId
          and rc.dayKey = :dayKey
          and rc.cascadaType = :cascadaType
    """)
    List<Long> findRecipientUserIdsByDayKey(
            @Param("plantId") Long plantId,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("shiftId") String shiftId,
            @Param("dayKey") String dayKey,
            @Param("cascadaType") CascadaType cascadaType
    );

    List<CascadaRecipient> findByRecipientUserIdAndCascadaType(Long recipientUserId, CascadaType cascadaType);
}
