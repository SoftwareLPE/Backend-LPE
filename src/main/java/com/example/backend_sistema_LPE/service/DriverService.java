package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.model.Driver;

import java.util.List;
import java.util.Optional;

public interface DriverService {

    List<Driver> getAllDrivers();

    Optional<Driver> getDriverById(Long driverId);

    List<DriverViewDTO> getDriversByPlant(Long plantId, Boolean active);

     void createDriver(CreateDriverWithRouteDTO driverCreateDTO);

    Driver updateDriver(Long driverId,Driver driver);

    void deleteDriver(Long driverId);

    Driver updateDriverActive(Long driverId, Boolean active);

    Driver updateDriverShifts(Long driverId, java.util.Set<Long> shiftIds);

    java.util.List<com.example.backend_sistema_LPE.dto.DriverViewDTO> getDriversByShift(Long shiftId, Boolean active);
}
