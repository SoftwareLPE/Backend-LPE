package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverPlantAssignmentRepository extends JpaRepository<DriverPlantAssignment, Long> {
    List<DriverPlantAssignment> findByPlantPlantId(Long plantId);

    Optional<DriverPlantAssignment> findByDriverDriverIdAndPlantPlantId(Long driverId, Long plantId);

    Optional<DriverPlantAssignment> findByDriverDriverIdAndPlantPlantIdAndRouteRouteId(
            Long driverId,
            Long plantId,
            Long routeId
    );

    Optional<DriverPlantAssignment> findByDriverDriverIdAndPlantPlantIdAndRouteIsNull(
            Long driverId,
            Long plantId
    );
}
