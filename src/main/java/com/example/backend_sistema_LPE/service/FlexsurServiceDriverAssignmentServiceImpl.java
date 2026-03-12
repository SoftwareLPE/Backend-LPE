package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateFlexsurServiceDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDriverAssignmentDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurServiceDriverAssignmentRequestDTO;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.FlexsurServiceDriverAssignment;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.FlexsurServiceDriverAssignmentRepository;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlexsurServiceDriverAssignmentServiceImpl implements FlexsurServiceDriverAssignmentService {
    private final FlexsurServiceDriverAssignmentRepository assignmentRepository;
    private final FlexsurServiceRepository flexsurServiceRepository;
    private final DriverRepository driverRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final PlantRepository plantRepository;

    public FlexsurServiceDriverAssignmentServiceImpl(
            FlexsurServiceDriverAssignmentRepository assignmentRepository,
            FlexsurServiceRepository flexsurServiceRepository,
            DriverRepository driverRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            PlantRepository plantRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.driverRepository = driverRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    public List<FlexsurServiceDriverAssignmentDTO> getAssignments(Long plantId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }

        List<FlexsurService> services = flexsurServiceRepository
                .findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId);
        Map<Long, FlexsurServiceDriverAssignment> assignmentsByServiceId = new LinkedHashMap<>();
        for (FlexsurServiceDriverAssignment assignment : assignmentRepository.findByPlantPlantId(plantId)) {
            if (assignment.getService() == null || assignment.getService().getServiceId() == null) {
                continue;
            }
            assignmentsByServiceId.put(assignment.getService().getServiceId(), assignment);
        }

        return services.stream()
                .map(service -> toDTO(service, assignmentsByServiceId.get(service.getServiceId())))
                .toList();
    }

    @Override
    @Transactional
    public FlexsurServiceDriverAssignmentDTO createAssignment(
            CreateFlexsurServiceDriverAssignmentRequestDTO request,
            Long userId
    ) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getServiceId() == null) {
            throw new RuntimeException("serviceId is required");
        }
        if (request.getDriverId() == null) {
            throw new RuntimeException("driverId is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        FlexsurService service = flexsurServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));
        if (!service.getPlant().getPlantId().equals(request.getPlantId())) {
            throw new RuntimeException("serviceId does not belong to plant");
        }

        Driver driver = validateDriverBelongsToPlant(request.getDriverId(), request.getPlantId());

        FlexsurServiceDriverAssignment assignment = assignmentRepository
                .findByPlantPlantIdAndServiceServiceId(request.getPlantId(), request.getServiceId())
                .orElseGet(FlexsurServiceDriverAssignment::new);
        assignment.setPlant(plant);
        assignment.setService(service);
        assignment.setDriver(driver);
        assignment.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setUpdatedByUserId(userId);

        return toDTO(service, assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public FlexsurServiceDriverAssignmentDTO updateAssignment(
            Long assignmentId,
            UpdateFlexsurServiceDriverAssignmentRequestDTO request,
            Long userId
    ) {
        if (assignmentId == null) {
            throw new RuntimeException("assignmentId is required");
        }

        FlexsurServiceDriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (request.getDriverId() != null) {
            Driver driver = validateDriverBelongsToPlant(
                    request.getDriverId(),
                    assignment.getPlant().getPlantId()
            );
            assignment.setDriver(driver);
        }
        if (request.getActive() != null) {
            assignment.setActive(request.getActive());
        }
        assignment.setUpdatedAt(LocalDateTime.now());
        assignment.setUpdatedByUserId(userId);

        FlexsurServiceDriverAssignment saved = assignmentRepository.save(assignment);
        return toDTO(saved.getService(), saved);
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

    private Driver validateDriverBelongsToPlant(Long driverId, Long plantId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        DriverPlantAssignment assignment = driverPlantAssignmentRepository
                .findByDriverDriverIdAndPlantPlantId(driverId, plantId)
                .orElseThrow(() -> new RuntimeException("driverId does not belong to plant"));
        if (assignment.getDriver() == null || !assignment.getDriver().getDriverId().equals(driver.getDriverId())) {
            throw new RuntimeException("driverId does not belong to plant");
        }
        return driver;
    }

    private FlexsurServiceDriverAssignmentDTO toDTO(
            FlexsurService service,
            FlexsurServiceDriverAssignment assignment
    ) {
        Driver driver = assignment == null ? null : assignment.getDriver();
        return new FlexsurServiceDriverAssignmentDTO(
                assignment == null ? null : assignment.getFlexsurServiceDriverAssignmentId(),
                service.getPlant().getPlantId(),
                service.getServiceId(),
                service.getServiceName(),
                driver == null ? null : driver.getDriverId(),
                driver == null ? null : driver.getDriverName(),
                driver == null ? null : driver.getLastName(),
                assignment == null ? null : assignment.getActive()
        );
    }
}
