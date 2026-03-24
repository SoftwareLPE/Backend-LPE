package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateFlexsurDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurDriverAssignmentDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.enums.DriverType;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.FlexsurDriverAssignment;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.FlexsurDriverAssignmentRepository;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class FlexsurDriverAssignmentServiceImpl implements FlexsurDriverAssignmentService {
    private static final Set<String> VALID_DAY_KEYS = Set.of("lun", "mar", "mie", "jue", "vie", "sab", "dom");

    private final FlexsurDriverAssignmentRepository assignmentRepository;
    private final PlantRepository plantRepository;
    private final DriverRepository driverRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final FlexsurServiceRepository flexsurServiceRepository;
    private final ShiftRepository shiftRepository;
    private final RouteRepository routeRepository;

    public FlexsurDriverAssignmentServiceImpl(
            FlexsurDriverAssignmentRepository assignmentRepository,
            PlantRepository plantRepository,
            DriverRepository driverRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            FlexsurServiceRepository flexsurServiceRepository,
            ShiftRepository shiftRepository,
            RouteRepository routeRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.plantRepository = plantRepository;
        this.driverRepository = driverRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.shiftRepository = shiftRepository;
        this.routeRepository = routeRepository;
    }

    @Override
    public List<FlexsurDriverAssignmentDTO> getAssignments(Long plantId, Boolean active, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (shiftId != null) {
            validateShiftBelongsToPlant(shiftId, plantId);
        }
        return (shiftId == null
                ? assignmentRepository.findByPlantPlantId(plantId)
                : assignmentRepository.findByPlantPlantIdAndShiftShiftId(plantId, shiftId))
                .stream()
                .filter(assignment -> active == null || active.equals(assignment.getActive()))
                .sorted(java.util.Comparator
                        .comparing((FlexsurDriverAssignment assignment) -> assignment.getDriver().getDriverName(), java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(assignment -> assignment.getShift().getShiftName(), java.util.Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(assignment -> assignment.getService().getServiceName(), java.util.Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public FlexsurDriverAssignmentDTO createAssignment(CreateFlexsurDriverAssignmentRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getServiceId() == null) {
            throw new RuntimeException("serviceId is required");
        }
        if (request.getShiftId() == null) {
            throw new RuntimeException("shiftId is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        FlexsurService service = validateServiceBelongsToPlant(request.getServiceId(), request.getPlantId());
        Shift shift = validateShiftBelongsToPlant(request.getShiftId(), request.getPlantId());
        validateServiceMatchesShift(service, shift);
        Route route = resolveRoute(request.getRouteId(), request.getPlantId());
        String routeLocation = resolveRouteLocation(route, request.getRouteLocation());
        DriverType driverType = resolveDriverType(request.getDriverType());
        Set<String> normalizedDayKeys = normalizeDayKeys(request.getDayKeys(), true);

        Driver driver = resolveOrCreateDriver(
                request.getDriverId(),
                request.getDriverName(),
                request.getDriverLastName(),
                plant,
                shift
        );
        syncDriverPlantAssignment(driver, plant, route, driverType);

        FlexsurDriverAssignment assignment = assignmentRepository
                .findByPlantPlantIdAndDriverDriverIdAndServiceServiceIdAndShiftShiftId(
                        plant.getPlantId(),
                        driver.getDriverId(),
                        service.getServiceId(),
                        shift.getShiftId()
                )
                .orElseGet(FlexsurDriverAssignment::new);
        assignment.setPlant(plant);
        assignment.setDriver(driver);
        assignment.setService(service);
        assignment.setShift(shift);
        assignment.setRoute(route);
        assignment.setRouteLocation(routeLocation);
        assignment.setDriverType(driverType);
        assignment.setDayKeys(normalizedDayKeys);
        assignment.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setUpdatedByUserId(userId);

        return toDTO(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public FlexsurDriverAssignmentDTO updateAssignment(Long assignmentId, UpdateFlexsurDriverAssignmentRequestDTO request, Long userId) {
        if (assignmentId == null) {
            throw new RuntimeException("assignmentId is required");
        }

        FlexsurDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        Plant plant = assignment.getPlant();
        Shift shift = assignment.getShift();
        if (request.getShiftId() != null) {
            shift = validateShiftBelongsToPlant(request.getShiftId(), plant.getPlantId());
        }

        Driver driver = assignment.getDriver();
        if (request.getDriverId() != null) {
            driver = validateDriverBelongsToPlant(request.getDriverId(), plant.getPlantId());
        } else if (trimToNull(request.getDriverName()) != null || request.getDriverLastName() != null) {
            if (trimToNull(request.getDriverName()) != null) {
                driver.setDriverName(trimToNull(request.getDriverName()));
            }
            if (request.getDriverLastName() != null) {
                driver.setLastName(trimToNull(request.getDriverLastName()));
            }
            driverRepository.save(driver);
        }
        ensureDriverHasShift(driver, shift);

        FlexsurService service = assignment.getService();
        if (request.getServiceId() != null) {
            service = validateServiceBelongsToPlant(request.getServiceId(), plant.getPlantId());
        }
        validateServiceMatchesShift(service, shift);

        Route route = assignment.getRoute();
        if (request.getRouteId() != null) {
            route = resolveRoute(request.getRouteId(), plant.getPlantId());
        }
        String routeLocation = assignment.getRouteLocation();
        if (request.getRouteLocation() != null) {
            route = null;
            routeLocation = trimToNull(request.getRouteLocation());
        } else if (request.getRouteId() != null) {
            routeLocation = resolveRouteLocation(route, null);
        }

        DriverType driverType = request.getDriverType() == null
                ? assignment.getDriverType()
                : resolveDriverType(request.getDriverType());
        syncDriverPlantAssignment(driver, plant, route, driverType);

        assignment.setDriver(driver);
        assignment.setService(service);
        assignment.setShift(shift);
        assignment.setRoute(route);
        assignment.setRouteLocation(routeLocation);
        assignment.setDriverType(driverType);
        if (request.getDayKeys() != null) {
            assignment.setDayKeys(normalizeDayKeys(request.getDayKeys(), true));
        }
        if (request.getActive() != null) {
            assignment.setActive(request.getActive());
        }
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setUpdatedByUserId(userId);

        return toDTO(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Long assignmentId) {
        if (assignmentId == null) {
            throw new RuntimeException("assignmentId is required");
        }
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new RuntimeException("Assignment not found");
        }
        assignmentRepository.deleteById(assignmentId);
    }

    private FlexsurService validateServiceBelongsToPlant(Long serviceId, Long plantId) {
        FlexsurService service = flexsurServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));
        if (!service.getPlant().getPlantId().equals(plantId)) {
            throw new RuntimeException("serviceId does not belong to plant");
        }
        return service;
    }

    private Shift validateShiftBelongsToPlant(Long shiftId, Long plantId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (!shift.getPlant().getPlantId().equals(plantId)) {
            throw new RuntimeException("shiftId does not belong to plant");
        }
        return shift;
    }

    private Route resolveRoute(Long routeId, Long plantId) {
        if (routeId == null) {
            return null;
        }
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));
        if (!route.getPlant().getPlantId().equals(plantId)) {
            throw new RuntimeException("routeId does not belong to plant");
        }
        return route;
    }

    private String resolveRouteLocation(Route route, String requestedRouteLocation) {
        if (route != null) {
            return trimToNull(route.getLocation());
        }
        return trimToNull(requestedRouteLocation);
    }

    private void validateServiceMatchesShift(FlexsurService service, Shift shift) {
        if (service == null || shift == null || service.getShift() == null) {
            return;
        }
        if (!shift.getShiftId().equals(service.getShift().getShiftId())) {
            throw new RuntimeException("serviceId does not belong to shiftId");
        }
    }

    private Driver resolveOrCreateDriver(
            Long driverId,
            String driverName,
            String driverLastName,
            Plant plant,
            Shift shift
    ) {
        if (driverId != null) {
            Driver driver = validateDriverBelongsToPlant(driverId, plant.getPlantId());
            ensureDriverHasShift(driver, shift);
            return driver;
        }

        String normalizedDriverName = trimToNull(driverName);
        if (normalizedDriverName == null) {
            throw new RuntimeException("driverName is required");
        }

        Driver driver = new Driver();
        driver.setDriverName(normalizedDriverName);
        driver.setLastName(trimToNull(driverLastName));
        driver.setPlant(plant);
        driver.setActive(Boolean.TRUE);
        driver.getShifts().add(shift);
        return driverRepository.save(driver);
    }

    private Driver validateDriverBelongsToPlant(Long driverId, Long plantId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        if (driver.getPlant() != null && plantId.equals(driver.getPlant().getPlantId())) {
            return driver;
        }
        driverPlantAssignmentRepository.findByDriverDriverIdAndPlantPlantId(driverId, plantId)
                .orElseThrow(() -> new RuntimeException("driverId does not belong to plant"));
        return driver;
    }

    private void ensureDriverHasShift(Driver driver, Shift shift) {
        if (driver.getShifts() == null) {
            driver.setShifts(new LinkedHashSet<>());
        }
        driver.getShifts().add(shift);
        driverRepository.save(driver);
    }

    private void syncDriverPlantAssignment(Driver driver, Plant plant, Route route, DriverType driverType) {
        DriverPlantAssignment assignment = driverPlantAssignmentRepository
                .findByDriverDriverIdAndPlantPlantId(driver.getDriverId(), plant.getPlantId())
                .orElseGet(DriverPlantAssignment::new);
        assignment.setDriver(driver);
        assignment.setPlant(plant);
        assignment.setRoute(route);
        assignment.setDriverType(driverType);
        driverPlantAssignmentRepository.save(assignment);
    }

    private DriverType resolveDriverType(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new RuntimeException("driverType is required");
        }
        String upper = normalized.toUpperCase(Locale.ROOT);
        return switch (upper) {
            case "REGULAR", "TITULAR" -> DriverType.TITULAR;
            case "EXTRA" -> DriverType.EXTRA;
            default -> throw new RuntimeException("Invalid driverType: " + value);
        };
    }

    private Set<String> normalizeDayKeys(Set<String> dayKeys, boolean required) {
        if ((dayKeys == null || dayKeys.isEmpty())) {
            if (required) {
                throw new RuntimeException("dayKeys is required");
            }
            return new LinkedHashSet<>();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String dayKey : dayKeys) {
            String value = trimToNull(dayKey);
            if (value == null) {
                continue;
            }
            String lower = value.toLowerCase(Locale.ROOT);
            if (!VALID_DAY_KEYS.contains(lower)) {
                throw new RuntimeException("Invalid dayKey: " + dayKey);
            }
            normalized.add(lower);
        }
        if (required && normalized.isEmpty()) {
            throw new RuntimeException("dayKeys is required");
        }
        return normalized;
    }

    private FlexsurDriverAssignmentDTO toDTO(FlexsurDriverAssignment assignment) {
        Driver driver = assignment.getDriver();
        FlexsurService service = assignment.getService();
        Shift shift = assignment.getShift();
        Route route = assignment.getRoute();
        return new FlexsurDriverAssignmentDTO(
                assignment.getFlexsurDriverAssignmentId(),
                assignment.getPlant().getPlantId(),
                driver == null ? null : driver.getDriverId(),
                driver == null ? null : driver.getDriverName(),
                driver == null ? null : driver.getLastName(),
                assignment.getDriverType() == DriverType.TITULAR
                        ? "REGULAR"
                        : assignment.getDriverType() == null ? null : assignment.getDriverType().name(),
                service == null ? null : service.getServiceId(),
                service == null ? null : service.getServiceName(),
                shift == null ? null : shift.getShiftId(),
                shift == null ? null : shift.getShiftName(),
                route == null ? null : route.getRouteId(),
                route == null ? null : route.getRouteName(),
                route == null ? assignment.getRouteLocation() : trimToNull(route.getLocation()),
                assignment.getDayKeys(),
                assignment.getActive()
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
