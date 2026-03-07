package com.example.backend_sistema_LPE.service;


import com.example.backend_sistema_LPE.dto.CreateDriverWithRouteDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.dto.UpdateDriverDTO;
import com.example.backend_sistema_LPE.enums.DriverType;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.DriverRoute;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;


@Service
public class DriverRouteServiceImpl implements DriverRouteService {
    private final DriverRepository driverRepository;
    private final PlantRepository plantRepository;
    private final RouteRepository routeRepository;
    private final DriverRouteRepository driverRouteRepository;
    private final ShiftRepository shiftRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;

    public DriverRouteServiceImpl(DriverRepository driverRepository, PlantRepository plantRepository, RouteRepository routeRepository, DriverRouteRepository driverRouteRepository, ShiftRepository shiftRepository, DriverPlantAssignmentRepository driverPlantAssignmentRepository) {
        this.driverRepository = driverRepository;
        this.plantRepository = plantRepository;
        this.routeRepository = routeRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.shiftRepository = shiftRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
    }


    @Override
    @Transactional
    public DriverRoute createDriverWithRouteAndAssignment(CreateDriverWithRouteDTO createDriverWithRouteDTO) {
        // 1. Planta
        Plant plant = plantRepository.findById(createDriverWithRouteDTO.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found with id: " + createDriverWithRouteDTO.getPlantId()));

        // 2. Crear chofer
        if (createDriverWithRouteDTO.getLastName() == null
                || createDriverWithRouteDTO.getLastName().trim().isBlank()) {
            throw new RuntimeException("Last name is required");
        }
        Driver driver = new Driver();
        driver.setDriverName(createDriverWithRouteDTO.getDriverName());
        driver.setLastName(createDriverWithRouteDTO.getLastName().trim());
        driver.setPlant(plant);
        driver.setActive(createDriverWithRouteDTO.getActive() == null ? Boolean.TRUE : createDriverWithRouteDTO.getActive());
        if (createDriverWithRouteDTO.getShiftIds() != null && !createDriverWithRouteDTO.getShiftIds().isEmpty()) {
            Iterable<Shift> shifts = shiftRepository.findAllById(createDriverWithRouteDTO.getShiftIds());
            java.util.HashSet<Shift> shiftSet = new java.util.HashSet<>();
            for (Shift shift : shifts) {
                shiftSet.add(shift);
            }
            driver.setShifts(shiftSet);
        }

        Driver savedDriver = driverRepository.save(driver);

        // 3. Resolver la ruta: por id o por nombre (opcional para EXTRA)
        Route route = null;
        DriverType type = createDriverWithRouteDTO.getDriverType(); // TITULAR o EXTRA

        // Si viene routeId, siempre intentamos cargar la ruta (para cualquier tipo)
        if (createDriverWithRouteDTO.getRouteId() != null) {
            route = routeRepository.findById(createDriverWithRouteDTO.getRouteId())
                    .orElseThrow(() -> new RuntimeException(
                            "Route not found with id: " + createDriverWithRouteDTO.getRouteId()
                    ));
            if (!route.getPlant().getPlantId().equals(plant.getPlantId())) {
                throw new RuntimeException("Route does not belong to plant");
            }
            if (createDriverWithRouteDTO.getUnitType() != null) {
                route.setUnitType(trimToNull(createDriverWithRouteDTO.getUnitType()));
                routeRepository.save(route);
            }

        } else {
            // No hay routeId, vemos el nombre de la ruta
            String routeName = createDriverWithRouteDTO.getRouteName();

            if (routeName != null && !routeName.isBlank()) {
                // Hay nombre de ruta: la buscamos o creamos (aplica para TITULAR y EXTRA)
                route = routeRepository.findByRouteNameAndPlantPlantId(routeName, plant.getPlantId())
                        .orElseGet(() -> {
                            Route newRoute = new Route();
                            newRoute.setRouteName(routeName);
                            newRoute.setLocation(trimToNull(createDriverWithRouteDTO.getRouteLocation()));
                            newRoute.setUnitType(trimToNull(createDriverWithRouteDTO.getUnitType()));
                            newRoute.setPlant(plant);
                            return routeRepository.save(newRoute);
                        });
                if (createDriverWithRouteDTO.getUnitType() != null) {
                    route.setUnitType(trimToNull(createDriverWithRouteDTO.getUnitType()));
                    routeRepository.save(route);
                }

            } else {
                // No hay ni routeId ni routeName

                if (type == DriverType.TITULAR) {
                    // Para TITULAR: recorrido obligatorio
                    throw new RuntimeException(
                            "Un chofer TITULAR debe tener un recorrido (routeId o routeName)."
                    );
                }
                // Para EXTRA: se permite que la ruta sea null
                // route se queda en null y continuamos
            }
        }

        // Permit same route to be assigned to multiple drivers.

        DriverPlantAssignment assignment = driverPlantAssignmentRepository
                .findByDriverDriverIdAndPlantPlantId(savedDriver.getDriverId(), plant.getPlantId())
                .orElseGet(DriverPlantAssignment::new);
        assignment.setDriver(savedDriver);
        assignment.setPlant(plant);
        assignment.setRoute(route);
        assignment.setDriverType(type);
        driverPlantAssignmentRepository.save(assignment);

        // 4. Crear registro en tabla intermedia
        DriverRoute driverRoute = new DriverRoute();
        driverRoute.setDriver(savedDriver);
        driverRoute.setRoute(route); // puede ser null si es EXTRA sin recorrido
        driverRoute.setDriverType(type);
//    driverRoute.setShift(createDriverWithRouteDTO.getShift());
//    driverRoute.setAssigmentDate(new Date());
//    driverRoute.setNotes(createDriverWithRouteDTO.getNotes());

        return driverRouteRepository.save(driverRoute);
    }

    @Override
    @Transactional
    public DriverViewDTO updateDriverWithAssignment(Long driverId, UpdateDriverDTO updateDriverDTO) {
        //Buscar Driver
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(()->new RuntimeException("Chofer no encontrado"+ driverId));

        //Actualizar nombre y apellidos del chofer
        if (updateDriverDTO.getDriverName() != null && !updateDriverDTO.getDriverName().isBlank()) {
            driver.setDriverName(updateDriverDTO.getDriverName().trim());
        }
        if (updateDriverDTO.getLastName() != null) {
            if (updateDriverDTO.getLastName().isBlank()) {
                throw new RuntimeException("Last name is required");
            }
            driver.setLastName(updateDriverDTO.getLastName().trim());
        }
        if (updateDriverDTO.getActive() != null) {
            driver.setActive(updateDriverDTO.getActive());
        }
        if (updateDriverDTO.getShiftIds() != null) {
            Iterable<Shift> shifts = shiftRepository.findAllById(updateDriverDTO.getShiftIds());
            java.util.HashSet<Shift> shiftSet = new java.util.HashSet<>();
            for (Shift shift : shifts) {
                shiftSet.add(shift);
            }
            driver.setShifts(shiftSet);
        }

       // Buscar la asignación actual (última) o crear nueva
        DriverRoute driverAssignment = driverRouteRepository.findTopByDriverDriverIdOrderByDriverRouteIdDesc(driverId)
                .orElseGet(()->{
                    DriverRoute driverRoute = new DriverRoute();
                    driverRoute.setDriver(driver);
                    return driverRoute;
                });

        // 4) Determinar driverType final
        // 4) Determinar driverType final
        DriverType finalType = updateDriverDTO.getDriverType() != null
                ? updateDriverDTO.getDriverType()
                : driverAssignment.getDriverType(); // si dto no trae tipo, conserva


        // Si todavía es null (caso raro: no había assignment y no mandaron driverType)
        if (finalType == null) {
            finalType = DriverType.EXTRA; // o el default que tú prefieras
        }

        // 5) Resolver ruta (puede ser null para EXTRA)
        Route route = driverAssignment.getRoute(); // valor actual por defecto

        boolean dtowithRouteId = updateDriverDTO.getRouteId() != null;
        boolean dtowithRouteName = updateDriverDTO.getRouteName() != null && !updateDriverDTO.getRouteName().isBlank();

        Long targetPlantId = updateDriverDTO.getPlantId() != null
                ? updateDriverDTO.getPlantId()
                : driver.getPlant().getPlantId();

        if (dtowithRouteId) {
            route = routeRepository.findById(updateDriverDTO.getRouteId())
                    .orElseThrow(() -> new RuntimeException("Route not found with id: " + updateDriverDTO.getRouteId()));
            if (!route.getPlant().getPlantId().equals(targetPlantId)) {
                throw new RuntimeException("Route does not belong to driver's plant");
            }
            if (updateDriverDTO.getRouteLocation() != null) {
                route.setLocation(trimToNull(updateDriverDTO.getRouteLocation()));
            }
            if (updateDriverDTO.getUnitType() != null) {
                route.setUnitType(trimToNull(updateDriverDTO.getUnitType()));
            }
            routeRepository.save(route);

        } else if (dtowithRouteName) {
            String routeName = updateDriverDTO.getRouteName().trim();
            route = routeRepository.findByRouteNameAndPlantPlantId(routeName, targetPlantId)
                    .orElseGet(() -> {
                        Route newRoute = new Route();
                        newRoute.setRouteName(routeName);
                        newRoute.setLocation(trimToNull(updateDriverDTO.getRouteLocation()));
                        newRoute.setUnitType(trimToNull(updateDriverDTO.getUnitType()));
                        newRoute.setPlant(plantRepository.findById(targetPlantId)
                                .orElseThrow(() -> new RuntimeException("Plant not found")));
                        return routeRepository.save(newRoute);
                    });
            if (updateDriverDTO.getUnitType() != null) {
                route.setUnitType(trimToNull(updateDriverDTO.getUnitType()));
                routeRepository.save(route);
            }

        } else {
            // No mandaron routeId ni routeName:
            // - si cambiaron a EXTRA (o es EXTRA) => permitimos null
            // - si es TITULAR => ruta obligatoria (si NO tiene una ya asignada)
            if (finalType == DriverType.EXTRA) {
                route = null;
            } else if (finalType == DriverType.TITULAR) {
                if (route == null) {
                    throw new RuntimeException("Un chofer TITULAR debe tener un recorrido (routeId o routeName).");
                }
            }
        }

        // Permit same route to be assigned to multiple drivers.

        Long plantId = targetPlantId;
        DriverPlantAssignment assignment = driverPlantAssignmentRepository
                .findByDriverDriverIdAndPlantPlantId(driverId, plantId)
                .orElseGet(() -> {
                    DriverPlantAssignment created = new DriverPlantAssignment();
                    created.setDriver(driver);
                    created.setPlant(plantRepository.findById(plantId)
                            .orElseThrow(() -> new RuntimeException("Plant not found")));
                    return created;
                });
        assignment.setRoute(route);
        assignment.setDriverType(finalType);
        driverPlantAssignmentRepository.save(assignment);

        // 6) Persistir cambios
        driverRepository.save(driver);

        driverAssignment.setDriverType(finalType);
        driverAssignment.setRoute(route); // puede ser null si EXTRA
        DriverRoute savedAssignment = driverRouteRepository.save(driverAssignment);

        // 7) Responder DTO de vista
        String routeNameOut = (savedAssignment.getRoute() != null)
                ? savedAssignment.getRoute().getRouteName()
                : null;


        String routeLocationOut = (savedAssignment.getRoute() != null)
                ? savedAssignment.getRoute().getLocation()
                : null;
        String unitTypeOut = (savedAssignment.getRoute() != null)
                ? savedAssignment.getRoute().getUnitType()
                : null;
        return new DriverViewDTO(
                driver.getDriverId(),
                driver.getDriverName(),
                driver.getLastName(),
                driver.getActive(),
                driver.getShifts().stream().map(Shift::getShiftId).collect(java.util.stream.Collectors.toSet()),
                routeNameOut,
                routeLocationOut,
                unitTypeOut,
                savedAssignment.getDriverType()
        );
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}

