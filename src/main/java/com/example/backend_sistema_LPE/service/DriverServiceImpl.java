package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverRoute;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
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
    private final ShiftRepository shiftRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;

    public DriverServiceImpl(DriverRepository driverRepository, PlantRepository plantRepository, DriverRouteRepository driverRouteRepository, RouteRepository routeRepository, ShiftRepository shiftRepository, DriverPlantAssignmentRepository driverPlantAssignmentRepository) {
        this.driverRepository = driverRepository;
        this.plantRepository = plantRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.routeRepository = routeRepository;
        this.shiftRepository = shiftRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
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
        List<DriverPlantAssignment> assignments = driverPlantAssignmentRepository.findByPlantPlantId(plantId);

        return assignments.stream()
                .filter(a -> active == null || active.equals(a.getDriver().getActive()))
                .map(a -> {
                    Driver d = a.getDriver();
                    Route r = a.getRoute();

                    return new DriverViewDTO(
                            d.getDriverId(),
                            d.getDriverName(),
                            d.getLastName(),
                            d.getActive(),
                            d.getShifts().stream().map(Shift::getShiftId).collect(java.util.stream.Collectors.toSet()),
                            r != null ? r.getRouteName() : null,
                            a.getDriverType()
                    );
                })
                .sorted(java.util.Comparator.comparing(
                        DriverViewDTO::getRouteName,
                        java.util.Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .collect(Collectors.toList());
    }


    @Override
    public void createDriver(CreateDriverWithRouteDTO driverCreateDTO) {
        if (driverCreateDTO.getLastName() == null || driverCreateDTO.getLastName().trim().isBlank()) {
            throw new RuntimeException("Last name is required");
        }
        Plant plant = plantRepository.findById(driverCreateDTO.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Driver driver = new Driver();
        driver.setDriverName(driverCreateDTO.getDriverName());
        driver.setLastName(driverCreateDTO.getLastName().trim());
        driver.setPlant(plant);
        driver.setActive(driverCreateDTO.getActive() == null ? Boolean.TRUE : driverCreateDTO.getActive());

        if (driverCreateDTO.getShiftIds() != null && !driverCreateDTO.getShiftIds().isEmpty()) {
            Iterable<Shift> shifts = shiftRepository.findAllById(driverCreateDTO.getShiftIds());
            java.util.HashSet<Shift> shiftSet = new java.util.HashSet<>();
            for (Shift shift : shifts) {
                shiftSet.add(shift);
            }
            driver.setShifts(shiftSet);
        }

        driverRepository.save(driver);

        Route route = routeRepository.findById(driverCreateDTO.getRouteId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        if (!route.getPlant().getPlantId().equals(plant.getPlantId())) {
            throw new RuntimeException("Route does not belong to plant");
        }
        // Permit same route to be assigned to multiple drivers.

        DriverRoute driverRoute = new DriverRoute();
        driverRoute.setDriver(driver);
        driverRoute.setRoute(route);
        driverRoute.setDriverType(driverCreateDTO.getDriverType());

        driverRouteRepository.save(driverRoute);

        DriverPlantAssignment assignment = new DriverPlantAssignment();
        assignment.setDriver(driver);
        assignment.setPlant(plant);
        assignment.setRoute(route);
        assignment.setDriverType(driverCreateDTO.getDriverType());
        driverPlantAssignmentRepository.save(assignment);
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
        if (driver.getShifts() != null) {
            existingDriver.setShifts(driver.getShifts());
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

    @Override
    public Driver updateDriverShifts(Long driverId, java.util.Set<Long> shiftIds) {
        Driver existingDriver = driverRepository.findById(driverId).orElseThrow(() ->
                new RuntimeException("Chofer no encontrado con id: " + driverId));
        java.util.HashSet<Shift> shiftSet = new java.util.HashSet<>();
        if (shiftIds != null && !shiftIds.isEmpty()) {
            Iterable<Shift> shifts = shiftRepository.findAllById(shiftIds);
            for (Shift shift : shifts) {
                shiftSet.add(shift);
            }
        }
        existingDriver.setShifts(shiftSet);
        return driverRepository.save(existingDriver);
    }

    @Override
    public List<DriverViewDTO> getDriversByShift(Long shiftId, Boolean active) {
        List<Driver> drivers = driverRepository.findByShiftsShiftId(shiftId);
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        return drivers.stream()
                .filter(d -> active == null || active.equals(d.getActive()))
                .flatMap(d -> {
                    java.util.Optional<DriverPlantAssignment> plantAssignment =
                            driverPlantAssignmentRepository.findByDriverDriverIdAndPlantPlantId(
                                    d.getDriverId(),
                                    shift.getPlant().getPlantId()
                            );
                    if (plantAssignment.isEmpty()) {
                        return java.util.stream.Stream.of(new DriverViewDTO(
                                d.getDriverId(),
                                d.getDriverName(),
                                d.getLastName(),
                                d.getActive(),
                                d.getShifts().stream().map(Shift::getShiftId).collect(java.util.stream.Collectors.toSet()),
                                null,
                                null
                        ));
                    }
                    DriverPlantAssignment assignment = plantAssignment.get();
                    return java.util.stream.Stream.of(new DriverViewDTO(
                            d.getDriverId(),
                            d.getDriverName(),
                            d.getLastName(),
                            d.getActive(),
                            d.getShifts().stream().map(Shift::getShiftId).collect(java.util.stream.Collectors.toSet()),
                            assignment.getRoute() != null ? assignment.getRoute().getRouteName() : null,
                            assignment.getDriverType()
                    ));
                })
                .sorted(java.util.Comparator.comparing(
                        DriverViewDTO::getRouteName,
                        java.util.Comparator.nullsLast(String::compareToIgnoreCase)
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}


