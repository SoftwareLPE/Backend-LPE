package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FlexsurService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlexsurServiceRepository extends JpaRepository<FlexsurService, Long> {
    List<FlexsurService> findByPlantPlantIdOrderBySortOrderAscServiceNameAsc(Long plantId);

    List<FlexsurService> findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(Long plantId);

    Optional<FlexsurService> findByPlantPlantIdAndServiceNameIgnoreCase(Long plantId, String serviceName);
}
