package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FlexsurServiceCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceUpdateRequestDTO;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlexsurServiceServiceImpl implements FlexsurServiceService {
    private final FlexsurServiceRepository flexsurServiceRepository;
    private final PlantRepository plantRepository;

    public FlexsurServiceServiceImpl(
            FlexsurServiceRepository flexsurServiceRepository,
            PlantRepository plantRepository
    ) {
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    public List<FlexsurServiceDTO> getServices(Long plantId, Boolean active) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        List<FlexsurService> services = Boolean.TRUE.equals(active)
                ? flexsurServiceRepository.findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId)
                : flexsurServiceRepository.findByPlantPlantIdOrderBySortOrderAscServiceNameAsc(plantId);
        return services.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public FlexsurServiceDTO createService(FlexsurServiceCreateRequestDTO request) {
        if (request == null || request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getServiceName() == null || request.getServiceName().trim().isBlank()) {
            throw new RuntimeException("serviceName is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        String name = request.getServiceName().trim();
        flexsurServiceRepository.findByPlantPlantIdAndServiceNameIgnoreCase(request.getPlantId(), name)
                .ifPresent(existing -> {
                    throw new RuntimeException("serviceName already exists");
                });

        FlexsurService service = new FlexsurService();
        service.setPlant(plant);
        service.setServiceName(name);
        service.setSortOrder(request.getSortOrder());
        service.setActive(Boolean.TRUE);

        return toDTO(flexsurServiceRepository.save(service));
    }

    @Override
    @Transactional
    public FlexsurServiceDTO updateService(Long serviceId, FlexsurServiceUpdateRequestDTO request) {
        if (serviceId == null) {
            throw new RuntimeException("serviceId is required");
        }
        if (request == null) {
            throw new RuntimeException("request is required");
        }

        FlexsurService service = flexsurServiceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (request.getServiceName() != null && !request.getServiceName().trim().isBlank()) {
            String name = request.getServiceName().trim();
            flexsurServiceRepository.findByPlantPlantIdAndServiceNameIgnoreCase(
                            service.getPlant().getPlantId(),
                            name
                    )
                    .filter(existing -> !existing.getServiceId().equals(serviceId))
                    .ifPresent(existing -> {
                        throw new RuntimeException("serviceName already exists");
                    });
            service.setServiceName(name);
        }
        if (request.getSortOrder() != null) {
            service.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }

        return toDTO(flexsurServiceRepository.save(service));
    }

    @Override
    @Transactional
    public void deleteService(Long serviceId) {
        if (serviceId == null) {
            throw new RuntimeException("serviceId is required");
        }
        if (!flexsurServiceRepository.existsById(serviceId)) {
            throw new RuntimeException("Service not found");
        }
        flexsurServiceRepository.deleteById(serviceId);
    }

    private FlexsurServiceDTO toDTO(FlexsurService service) {
        return new FlexsurServiceDTO(
                service.getServiceId(),
                service.getPlant().getPlantId(),
                service.getServiceName(),
                service.getSortOrder(),
                service.getActive()
        );
    }
}
