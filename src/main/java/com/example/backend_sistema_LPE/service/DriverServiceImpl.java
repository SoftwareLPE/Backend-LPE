package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverRoute;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final PlantRepository plantRepository;
    private final DriverRouteRepository driverRouteRepository;
    private final RouteRepository routeRepository;

    public DriverServiceImpl(DriverRepository driverRepository, PlantRepository plantRepository, DriverRouteRepository driverRouteRepository, RouteRepository routeRepository) {
        this.driverRepository = driverRepository;
        this.plantRepository = plantRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @Override
    public Optional<Driver> getDriverById(Long driverId) {
        return driverRepository.findById(driverId);
    }

    @Override
    public List<DriverViewDTO> getDriversByPlant(Long plantId, Boolean active) {
        List<DriverRoute> assignments = driverRouteRepository.findByDriver_Plant_PlantId(plantId);

        return assignments.stream()
                .filter(dr -> active == null || active.equals(dr.getDriver().getActive()))
                .map(dr -> {
                    Driver d = dr.getDriver();
                    Route r = dr.getRoute();

                    return new DriverViewDTO(
                            d.getDriverId(),
                            d.getDriverName(),
                            d.getLastName(),
                            d.getActive(),
                            r != null ? r.getRouteName() : null,
                            dr.getDriverType()
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public void createDriver(CreateDriverWithRouteDTO driverCreateDTO) {
        Plant plant = plantRepository.findById(driverCreateDTO.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Driver driver = new Driver();
        driver.setDriverName(driverCreateDTO.getDriverName());
        driver.setLastName(driverCreateDTO.getLastName());
        driver.setPlant(plant);
        driver.setActive(driverCreateDTO.getActive() == null ? Boolean.TRUE : driverCreateDTO.getActive());

        driverRepository.save(driver);

        Route route = routeRepository.findById(driverCreateDTO.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        if (!route.getPlant().getPlantId().equals(plant.getPlantId())) {
            throw new RuntimeException("Route does not belong to plant");
        }
        if (driverRouteRepository.existsByRouteRouteId(route.getRouteId())) {
            throw new RuntimeException("Route already assigned to another driver");
        }

        DriverRoute driverRoute = new DriverRoute();
        driverRoute.setDriver(driver);
        driverRoute.setRoute(route);
        driverRoute.setDriverType(driverCreateDTO.getDriverType());

        driverRouteRepository.save(driverRoute);
    }


    @Override
    public Driver updateDriver(Long driverId, Driver driver) {
        Driver existingDriver = driverRepository.findById(driverId).orElseThrow(() ->
                new RuntimeException("Chofer no encontrado con id: " + driverId));

        existingDriver.setDriverName(driver.getDriverName());
        existingDriver.setLastName(driver.getLastName());
        if (driver.getActive() != null) {
            existingDriver.setActive(driver.getActive());
        }

        return driverRepository.save(existingDriver);
    }

    @Override
    public void deleteDriver(Long driverId) {

    }

    @Override
    public Driver updateDriverActive(Long driverId, Boolean active) {
        Driver existingDriver = driverRepository.findById(driverId).orElseThrow(() ->
                new RuntimeException("Chofer no encontrado con id: " + driverId));
        existingDriver.setActive(active);
        return driverRepository.save(existingDriver);
    }
}


