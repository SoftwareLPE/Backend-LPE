package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FlexsurServiceCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceUpdateRequestDTO;
import com.example.backend_sistema_LPE.enums.ShiftType;
import com.example.backend_sistema_LPE.enums.SpecialWeekType;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FlexsurServiceServiceImpl implements FlexsurServiceService {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final FlexsurServiceRepository flexsurServiceRepository;
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;

    public FlexsurServiceServiceImpl(
            FlexsurServiceRepository flexsurServiceRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository
    ) {
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public List<FlexsurServiceDTO> getServices(Long plantId, Boolean active, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (shiftId != null) {
            validateShiftBelongsToPlant(shiftId, plantId);
        }

        List<FlexsurService> services;
        if (shiftId != null) {
            services = Boolean.TRUE.equals(active)
                    ? flexsurServiceRepository.findByPlantPlantIdAndShiftShiftIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId, shiftId)
                    : flexsurServiceRepository.findByPlantPlantIdAndShiftShiftIdOrderBySortOrderAscServiceNameAsc(plantId, shiftId);
        } else {
            services = Boolean.TRUE.equals(active)
                    ? flexsurServiceRepository.findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId)
                    : flexsurServiceRepository.findByPlantPlantIdOrderBySortOrderAscServiceNameAsc(plantId);
        }
        return services.stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional
    public FlexsurServiceDTO createService(FlexsurServiceCreateRequestDTO request) {
        if (request == null || request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Shift shift = request.getShiftId() == null
                ? null
                : validateShiftBelongsToPlant(request.getShiftId(), request.getPlantId());
        String serviceType = trimToNull(request.getServiceType());
        LocalTime serviceTime = request.getServiceTime();
        SpecialWeekType specialWeekType = resolveSpecialWeekType(request.getSpecialWeekType(), shift);
        String name = resolveServiceName(request.getServiceName(), serviceType, shift, serviceTime, specialWeekType);

        validateServiceUniqueness(
                request.getPlantId(),
                null,
                name,
                serviceType,
                shift == null ? null : shift.getShiftId(),
                serviceTime,
                specialWeekType
        );

        FlexsurService service = new FlexsurService();
        service.setPlant(plant);
        service.setShift(shift);
        service.setServiceType(serviceType);
        service.setServiceTime(serviceTime);
        service.setSpecialWeekType(specialWeekType);
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

        Shift shift = service.getShift();
        if (request.getShiftId() != null) {
            shift = validateShiftBelongsToPlant(request.getShiftId(), service.getPlant().getPlantId());
        }
        String serviceType = request.getServiceType() != null
                ? trimToNull(request.getServiceType())
                : service.getServiceType();
        LocalTime serviceTime = request.getServiceTime() != null
                ? request.getServiceTime()
                : service.getServiceTime();
        SpecialWeekType specialWeekType = request.getSpecialWeekType() != null || request.getShiftId() != null
                ? resolveSpecialWeekType(request.getSpecialWeekType(), shift)
                : service.getSpecialWeekType();

        boolean shouldRegenerateName = request.getServiceName() != null
                || request.getServiceType() != null
                || request.getShiftId() != null
                || request.getServiceTime() != null
                || request.getSpecialWeekType() != null;
        if (shouldRegenerateName) {
            String serviceName = resolveServiceName(request.getServiceName(), serviceType, shift, serviceTime, specialWeekType);
            validateServiceUniqueness(
                    service.getPlant().getPlantId(),
                    serviceId,
                    serviceName,
                    serviceType,
                    shift == null ? null : shift.getShiftId(),
                    serviceTime,
                    specialWeekType
            );
            service.setServiceName(serviceName);
        }

        service.setShift(shift);
        service.setServiceType(serviceType);
        service.setServiceTime(serviceTime);
        service.setSpecialWeekType(specialWeekType);
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
                service.getServiceType(),
                service.getShift() == null ? null : service.getShift().getShiftId(),
                service.getShift() == null ? null : service.getShift().getShiftName(),
                service.getServiceTime(),
                service.getSpecialWeekType() == null ? null : service.getSpecialWeekType().name(),
                service.getSortOrder(),
                service.getActive()
        );
    }

    private Shift validateShiftBelongsToPlant(Long shiftId, Long plantId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (shift.getPlant() == null || !plantId.equals(shift.getPlant().getPlantId())) {
            throw new RuntimeException("shiftId does not belong to plant");
        }
        return shift;
    }

    private String resolveServiceName(
            String requestedServiceName,
            String serviceType,
            Shift shift,
            LocalTime serviceTime,
            SpecialWeekType specialWeekType
    ) {
        if (serviceType != null || shift != null || serviceTime != null) {
            if (serviceType == null) {
                throw new RuntimeException("serviceType is required");
            }
            if (shift == null) {
                throw new RuntimeException("shiftId is required");
            }
            if (serviceTime == null) {
                throw new RuntimeException("serviceTime is required");
            }
            return buildServiceName(serviceType, shift, serviceTime, specialWeekType);
        }

        String serviceName = trimToNull(requestedServiceName);
        if (serviceName == null) {
            throw new RuntimeException("serviceName is required");
        }
        return serviceName;
    }

    private void validateServiceUniqueness(
            Long plantId,
            Long currentServiceId,
            String serviceName,
            String serviceType,
            Long shiftId,
            LocalTime serviceTime,
            SpecialWeekType specialWeekType
    ) {
        if (serviceType != null && shiftId != null && serviceTime != null) {
            java.util.Optional<FlexsurService> existingService = specialWeekType == null
                    ? flexsurServiceRepository.findByPlantPlantIdAndShiftShiftIdAndServiceTypeIgnoreCaseAndServiceTime(
                    plantId,
                    shiftId,
                    serviceType,
                    serviceTime
            )
                    : flexsurServiceRepository.findByPlantPlantIdAndShiftShiftIdAndServiceTypeIgnoreCaseAndServiceTimeAndSpecialWeekType(
                    plantId,
                    shiftId,
                    serviceType,
                    serviceTime,
                    specialWeekType
            );
            existingService
                    .filter(service -> !service.getServiceId().equals(currentServiceId))
                    .ifPresent(service -> {
                        throw new RuntimeException("service already exists for shift and time");
                    });
            return;
        }

        flexsurServiceRepository.findByPlantPlantIdAndServiceNameIgnoreCase(plantId, serviceName)
                .filter(existing -> !existing.getServiceId().equals(currentServiceId))
                .ifPresent(existing -> {
                    throw new RuntimeException("serviceName already exists");
                });
    }

    private String buildServiceName(String serviceType, Shift shift, LocalTime serviceTime, SpecialWeekType specialWeekType) {
        StringBuilder builder = new StringBuilder()
                .append(serviceType.trim())
                .append(" ")
                .append(shift.getShiftName().trim())
                .append(" ")
                .append(serviceTime.format(TIME_FORMATTER));
        if (specialWeekType == SpecialWeekType.LONG) {
            builder.append(" Semana Larga");
        } else if (specialWeekType == SpecialWeekType.SHORT) {
            builder.append(" Semana Corta");
        }
        return builder.toString();
    }

    private SpecialWeekType resolveSpecialWeekType(String requestedValue, Shift shift) {
        ShiftType shiftType = shift == null || shift.getShiftType() == null
                ? ShiftType.REGULAR
                : shift.getShiftType();
        String value = trimToNull(requestedValue);

        if (shiftType == ShiftType.SPECIAL) {
            if (value == null) {
                throw new RuntimeException("specialWeekType is required for SPECIAL shift");
            }
            try {
                return SpecialWeekType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid specialWeekType: " + requestedValue);
            }
        }

        if (value != null) {
            throw new RuntimeException("specialWeekType only applies to SPECIAL shifts");
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
