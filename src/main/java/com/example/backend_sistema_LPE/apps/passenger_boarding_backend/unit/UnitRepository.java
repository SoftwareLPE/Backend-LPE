package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    Optional<Unit> findByPlantPlantIdAndWialonId(Long plantId, Long wialonId);

    List<Unit> findAllByPlantPlantIdAndIsActiveTrueOrderByNameRawAsc(Long plantId);

    List<Unit> findAllByPlantPlantId(Long plantId);

    long countByPlantPlantIdAndIsActiveTrue(Long plantId);
}
