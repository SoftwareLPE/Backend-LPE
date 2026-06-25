package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Collection;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;

@Repository
public interface BoardingEventRepository extends JpaRepository<BoardingEvent, Long>, JpaSpecificationExecutor<BoardingEvent> {
    boolean existsByWialonRowKey(String wialonRowKey);

    List<BoardingEvent> findByPlantPlantIdAndBoardingTimeBetween(
            Long plantId,
            Timestamp from,
            Timestamp to,
            Sort sort
    );

    long countByUnitUnitId(Long unitId);

    long countByUnitUnitIdAndShiftIn(Long unitId, Collection<String> shifts);

    @Query("""
        select distinct be.unit
        from BoardingEvent be
        where be.plant.plantId = :plantId
          and be.unit.isActive = true
          and (:from is null or be.boardingTime >= :from)
          and (:to is null or be.boardingTime <= :to)
    """)
    List<Unit> findDistinctUnitsByPlantAndBoardingTimeBetween(
            @Param("plantId") Long plantId,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to
    );

    @Query("""
        select distinct be.unit
        from BoardingEvent be
        where be.plant.plantId = :plantId
          and be.boardingTime >= :from
          and be.boardingTime <= :to
    """)
    List<Unit> findDistinctUnitsWithEventsByPlantAndBoardingTimeBetween(
            @Param("plantId") Long plantId,
            @Param("from") Timestamp from,
            @Param("to") Timestamp to
    );
}
