package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalTripTypeCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeUpdateRequestDTO;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.RegalTripType;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RegalTripTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegalTripTypeServiceImpl implements RegalTripTypeService {
    private final RegalTripTypeRepository regalTripTypeRepository;
    private final PlantRepository plantRepository;

    public RegalTripTypeServiceImpl(
            RegalTripTypeRepository regalTripTypeRepository,
            PlantRepository plantRepository
    ) {
        this.regalTripTypeRepository = regalTripTypeRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    public List<RegalTripTypeDTO> getTripTypes(Long plantId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        return regalTripTypeRepository.findByPlantPlantIdAndActiveTrueOrderBySortOrderAsc(plantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public RegalTripTypeDTO createTripType(Long plantId, RegalTripTypeCreateRequestDTO request) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getLabel() == null || request.getLabel().trim().isBlank()) {
            throw new RuntimeException("label is required");
        }
        if (request.getDayKeys() == null || request.getDayKeys().isEmpty()) {
            throw new RuntimeException("dayKeys is required");
        }

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        RegalTripType tripType = new RegalTripType();
        tripType.setPlant(plant);
        if (request.getCode() != null && !request.getCode().trim().isBlank()) {
            tripType.setCode(request.getCode().trim().toUpperCase());
        } else {
            tripType.setCode(slugify(request.getLabel()));
        }
        tripType.setLabel(request.getLabel().trim());
        tripType.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        tripType.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        tripType.setDayKeys(normalizeDayKeys(request.getDayKeys()));

        return toDTO(regalTripTypeRepository.save(tripType));
    }

    @Override
    @Transactional
    public RegalTripTypeDTO updateTripType(Long plantId, Long tripTypeId, RegalTripTypeUpdateRequestDTO request) {
        RegalTripType tripType = regalTripTypeRepository.findByTripTypeIdAndPlantPlantId(tripTypeId, plantId)
                .orElseThrow(() -> new RuntimeException("Trip type not found"));

        if (request.getCode() != null && !request.getCode().trim().isBlank()) {
            tripType.setCode(request.getCode().trim().toUpperCase());
        }
        if (request.getLabel() != null && !request.getLabel().trim().isBlank()) {
            tripType.setLabel(request.getLabel().trim());
        }
        if (request.getSortOrder() != null) {
            tripType.setSortOrder(request.getSortOrder());
        }
        if (request.getActive() != null) {
            tripType.setActive(request.getActive());
        }
        if (request.getDayKeys() != null && !request.getDayKeys().isEmpty()) {
            tripType.setDayKeys(normalizeDayKeys(request.getDayKeys()));
        }

        return toDTO(regalTripTypeRepository.save(tripType));
    }

    @Override
    @Transactional
    public void deactivateTripType(Long plantId, Long tripTypeId) {
        RegalTripType tripType = regalTripTypeRepository.findByTripTypeIdAndPlantPlantId(tripTypeId, plantId)
                .orElseThrow(() -> new RuntimeException("Trip type not found"));
        tripType.setActive(false);
        regalTripTypeRepository.save(tripType);
    }

    private RegalTripTypeDTO toDTO(RegalTripType tripType) {
        return new RegalTripTypeDTO(
                tripType.getTripTypeId(),
                tripType.getCode(),
                tripType.getLabel(),
                tripType.getSortOrder(),
                tripType.getActive(),
                tripType.getDayKeys()
        );
    }

    private java.util.Set<String> normalizeDayKeys(java.util.Set<String> dayKeys) {
        java.util.Set<String> normalized = new java.util.HashSet<>();
        for (String dayKey : dayKeys) {
            if (dayKey == null || dayKey.isBlank()) {
                continue;
            }
            normalized.add(dayKey.trim().toLowerCase());
        }
        return normalized;
    }

    private String slugify(String label) {
        if (label == null) {
            return "TRIP_TYPE";
        }
        return label.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
