package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapDTO;
import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapSaveRequestDTO;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.ShiftFormatTurnMap;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftFormatTurnMapRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShiftFormatTurnMapServiceImpl implements ShiftFormatTurnMapService {
    private final ShiftFormatTurnMapRepository mapRepository;
    private final PlantRepository plantRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final ShiftRepository shiftRepository;

    public ShiftFormatTurnMapServiceImpl(
            ShiftFormatTurnMapRepository mapRepository,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            ShiftRepository shiftRepository
    ) {
        this.mapRepository = mapRepository;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public List<ShiftFormatTurnMapDTO> getMappings(Long plantId, Long formatTypeId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }

        return mapRepository.findByPlantPlantIdAndFormatTypeFormatTypeId(plantId, formatTypeId).stream()
                .map(map -> new ShiftFormatTurnMapDTO(
                        map.getShift().getShiftId(),
                        map.getDayOfWeek(),
                        map.getTurnName()
                ))
                .toList();
    }

    @Override
    @Transactional
    public void saveMappings(ShiftFormatTurnMapSaveRequestDTO request) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getFormatTypeId() == null) {
            throw new RuntimeException("formatTypeId is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        FormatType formatType = formatTypeRepository.findById(request.getFormatTypeId())
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        mapRepository.deleteByPlantPlantIdAndFormatTypeFormatTypeId(
                request.getPlantId(),
                request.getFormatTypeId()
        );

        List<ShiftFormatTurnMapDTO> mappings = request.getMappings() == null
                ? List.of()
                : request.getMappings();

        List<ShiftFormatTurnMap> toSave = new ArrayList<>();
        for (ShiftFormatTurnMapDTO mapping : mappings) {
            if (mapping.getShiftId() == null) {
                throw new RuntimeException("shiftId is required");
            }
            if (mapping.getDayOfWeek() == null || mapping.getDayOfWeek().trim().isBlank()) {
                throw new RuntimeException("dayOfWeek is required");
            }
            if (mapping.getTurnName() == null || mapping.getTurnName().trim().isBlank()) {
                throw new RuntimeException("turnName is required");
            }

            Shift shift = shiftRepository.findById(mapping.getShiftId())
                    .orElseThrow(() -> new RuntimeException("Shift not found: " + mapping.getShiftId()));

            ShiftFormatTurnMap map = new ShiftFormatTurnMap();
            map.setPlant(plant);
            map.setFormatType(formatType);
            map.setShift(shift);
            map.setDayOfWeek(mapping.getDayOfWeek().trim().toLowerCase());
            map.setTurnName(mapping.getTurnName().trim());
            toSave.add(map);
        }

        if (!toSave.isEmpty()) {
            mapRepository.saveAll(toSave);
        }
    }
}
