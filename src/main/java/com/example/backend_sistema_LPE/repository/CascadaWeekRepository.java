package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.CascadaWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CascadaWeekRepository extends JpaRepository<CascadaWeek, Long> {
    Optional<CascadaWeek> findByPlantPlantIdAndWeekStartDateAndShiftId(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId
    );

    List<CascadaWeek> findByPlantPlantIdAndWeekStartDateAndStatus(
            Long plantId,
            LocalDate weekStartDate,
            CascadaStatus status
    );

    @Modifying
    @Query("""
        update CascadaWeek cw
        set cw.status = :status,
            cw.sentAt = :sentAt,
            cw.sentByUserId = :sentByUserId,
            cw.updatedAt = :updatedAt,
            cw.updatedByUserId = :updatedByUserId
        where cw.plant.plantId = :plantId
          and cw.weekStartDate = :weekStartDate
          and cw.shiftId = :shiftId
    """)
    int updateStatusByPlantAndWeekAndShift(
            @Param("plantId") Long plantId,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("shiftId") String shiftId,
            @Param("status") CascadaStatus status,
            @Param("sentAt") LocalDateTime sentAt,
            @Param("sentByUserId") Long sentByUserId,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("updatedByUserId") Long updatedByUserId
    );

    @Query("""
        select cw
        from CascadaWeek cw
        join cw.plant p
        join CascadaRecipient rc on rc.plant.plantId = p.plantId
            and rc.weekStartDate = cw.weekStartDate
            and rc.shiftId = cw.shiftId
        where cw.status = :status
          and (:plantId is null or p.plantId = :plantId)
          and (:weekStartDate is null or cw.weekStartDate = :weekStartDate)
          and (:recipientUserId is null or rc.recipientUserId = :recipientUserId)
    """)
    List<CascadaWeek> findWeeksForSummary(
            @Param("status") CascadaStatus status,
            @Param("plantId") Long plantId,
            @Param("weekStartDate") LocalDate weekStartDate,
            @Param("recipientUserId") Long recipientUserId
    );

    void deleteByPlantPlantId(Long plantId);

    void deleteByPlantCompanyCompanyId(Long companyId);
}
