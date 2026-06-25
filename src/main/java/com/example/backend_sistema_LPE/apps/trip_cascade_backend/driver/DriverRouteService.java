package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

public interface DriverRouteService {

    DriverRoute createDriverWithRouteAndAssignment(
            CreateDriverWithRouteDTO createDriverWithRouteDTO
    );

    DriverViewDTO updateDriverWithAssignment(Long driverId, UpdateDriverDTO updateDriverDTO);


}
