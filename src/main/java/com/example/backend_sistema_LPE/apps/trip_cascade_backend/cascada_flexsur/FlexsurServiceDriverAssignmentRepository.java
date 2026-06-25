package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlexsurServiceDriverAssignmentRepository extends JpaRepository<FlexsurServiceDriverAssignment, Long> {
    List<FlexsurServiceDriverAssignment> findByPlantPlantId(Long plantId);

    Optional<FlexsurServiceDriverAssignment> findByPlantPlantIdAndServiceServiceId(Long plantId, Long serviceId);

    void deleteByPlantCompanyCompanyId(Long companyId);
}
