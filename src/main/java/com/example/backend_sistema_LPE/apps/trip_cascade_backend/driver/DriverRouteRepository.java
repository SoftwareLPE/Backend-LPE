package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRouteRepository extends JpaRepository<DriverRoute,Long> {
    List<DriverRoute> findByDriverDriverId(Long driverId);

    List<DriverRoute> findByRouteRouteId(Long routeId);

    List<DriverRoute> findByDriver_Plant_PlantId(Long plantId);

    Optional<DriverRoute> findTopByDriverDriverIdOrderByDriverRouteIdDesc(Long driverId);

    void deleteByDriverPlantCompanyCompanyId(Long companyId);

    void deleteByDriverPlantPlantId(Long plantId);

    boolean existsByRouteRouteId(Long routeId);

    boolean existsByRouteRouteIdAndDriverDriverIdNot(Long routeId, Long driverId);
}
