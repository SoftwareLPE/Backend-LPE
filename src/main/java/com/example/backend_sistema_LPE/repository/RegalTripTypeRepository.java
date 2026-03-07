package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.RegalTripType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegalTripTypeRepository extends JpaRepository<RegalTripType, Long> {
    List<RegalTripType> findByPlantPlantIdAndActiveTrueOrderBySortOrderAsc(Long plantId);

    Optional<RegalTripType> findByTripTypeIdAndPlantPlantId(Long tripTypeId, Long plantId);

    Optional<RegalTripType> findByPlantPlantIdAndCodeIgnoreCase(Long plantId, String code);
}
