package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateShiftRequestDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateShiftRequestDTO;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final PlantRepository plantRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final FormatTurnConfigRepository formatTurnConfigRepository;

    public ShiftServiceImpl(
            ShiftRepository shiftRepository,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            FormatTurnConfigRepository formatTurnConfigRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.formatTurnConfigRepository = formatTurnConfigRepository;
    }

    @Override
    public List<ShiftDTO> getShiftsByPlant(Long plantId) {
        return shiftRepository.findByPlantPlantId(plantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ShiftDTO createShift(Long plantId, CreateShiftRequestDTO request) {
        validateRequest(request);
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found " + plantId));

        Shift shift = new Shift();
        shift.setPlant(plant);
        shift.setShiftName(request.getShiftName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setDayKeys(request.getDayKeys());

        Shift saved = shiftRepository.save(shift);
        syncTurnConfigForShift(plant, saved.getShiftName(), saved.getDayKeys());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public ShiftDTO updateShift(Long plantId, Long shiftId, UpdateShiftRequestDTO request) {
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));

        String oldShiftName = shift.getShiftName();
        java.util.Set<String> oldDayKeys = shift.getDayKeys() == null
                ? java.util.Set.of()
                : new java.util.HashSet<>(shift.getDayKeys());

        boolean hasUpdates = false;

        if (request.getShiftName() != null && !request.getShiftName().trim().isBlank()) {
            shift.setShiftName(request.getShiftName().trim());
            hasUpdates = true;
        }
        if (request.getStartTime() != null) {
            shift.setStartTime(request.getStartTime());
            hasUpdates = true;
        }
        if (request.getEndTime() != null) {
            shift.setEndTime(request.getEndTime());
            hasUpdates = true;
        }
        if (request.getDayKeys() != null && !request.getDayKeys().isEmpty()) {
            shift.setDayKeys(request.getDayKeys());
            hasUpdates = true;
        }

        if (!hasUpdates) {
            return toDTO(shift);
        }

        Shift saved = shiftRepository.save(shift);
        syncTurnConfigForUpdate(saved.getPlant(), oldShiftName, oldDayKeys, saved.getShiftName(), saved.getDayKeys());
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteShift(Long plantId, Long shiftId) {
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));
        deleteTurnConfigForShift(shift.getPlant(), shift.getShiftName(), shift.getDayKeys());
        shiftRepository.delete(shift);
    }

    private ShiftDTO toDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setShiftId(shift.getShiftId());
        dto.setShiftName(shift.getShiftName());
        dto.setDayKeys(shift.getDayKeys());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        return dto;
    }

    private void validateRequest(CreateShiftRequestDTO request) {
        if (request.getShiftName() == null || request.getShiftName().trim().isBlank()) {
            throw new RuntimeException("shiftName is required");
        }
        if (request.getDayKeys() == null || request.getDayKeys().isEmpty()) {
            throw new RuntimeException("dayKeys is required");
        }
        // startTime/endTime are optional for admin trips
    }

    private void validateRequest(UpdateShiftRequestDTO request) {
    }

    private void syncTurnConfigForUpdate(
            Plant plant,
            String oldShiftName,
            java.util.Set<String> oldDayKeys,
            String newShiftName,
            java.util.Set<String> newDayKeys
    ) {
        String normalizedOldName = normalizeTurnName(oldShiftName);
        String normalizedNewName = normalizeTurnName(newShiftName);
        java.util.Set<String> oldDays = normalizeDayKeys(oldDayKeys);
        java.util.Set<String> newDays = normalizeDayKeys(newDayKeys);

        if (!normalizedOldName.equals(normalizedNewName)) {
            deleteTurnConfigForShift(plant, normalizedOldName, oldDays);
            syncTurnConfigForShift(plant, normalizedNewName, newDays);
            return;
        }

        java.util.Set<String> removedDays = new java.util.HashSet<>(oldDays);
        removedDays.removeAll(newDays);
        if (!removedDays.isEmpty()) {
            deleteTurnConfigForShift(plant, normalizedNewName, removedDays);
        }

        java.util.Set<String> addedDays = new java.util.HashSet<>(newDays);
        addedDays.removeAll(oldDays);
        if (!addedDays.isEmpty()) {
            syncTurnConfigForShift(plant, normalizedNewName, addedDays);
        }
    }

    private void syncTurnConfigForShift(Plant plant, String shiftName, java.util.Set<String> dayKeys) {
        FormatType formatType = resolveFormatType(plant);
        if (formatType == null) {
            return;
        }

        String normalizedName = normalizeTurnName(shiftName);
        java.util.Set<String> normalizedDays = normalizeDayKeys(dayKeys);

        for (String dayKey : normalizedDays) {
            if (formatTurnConfigRepository.findByFormatTypeFormatTypeIdAndDayOfWeekAndTurnName(
                    formatType.getFormatTypeId(),
                    dayKey,
                    normalizedName
            ).isPresent()) {
                continue;
            }

            FormatTurnConfig config = new FormatTurnConfig();
            config.setFormatType(formatType);
            config.setDayOfWeek(dayKey);
            config.setTurnName(normalizedName);
            config.setSortOrder(resolveSortOrder(normalizedName));
            formatTurnConfigRepository.save(config);
        }
    }

    private void deleteTurnConfigForShift(Plant plant, String shiftName, java.util.Set<String> dayKeys) {
        FormatType formatType = resolveFormatType(plant);
        if (formatType == null) {
            return;
        }

        String normalizedName = normalizeTurnName(shiftName);
        java.util.Set<String> normalizedDays = normalizeDayKeys(dayKeys);
        for (String dayKey : normalizedDays) {
            formatTurnConfigRepository.deleteByFormatTypeFormatTypeIdAndDayOfWeekAndTurnName(
                    formatType.getFormatTypeId(),
                    dayKey,
                    normalizedName
            );
        }
    }

    private FormatType resolveFormatType(Plant plant) {
        Long formatTypeId = plant.getFormatTypeId();
        if (formatTypeId == null) {
            return null;
        }
        return formatTypeRepository.findById(formatTypeId).orElse(null);
    }

    private java.util.Set<String> normalizeDayKeys(java.util.Set<String> dayKeys) {
        if (dayKeys == null || dayKeys.isEmpty()) {
            return java.util.Set.of();
        }
        java.util.Set<String> normalized = new java.util.HashSet<>();
        for (String dayKey : dayKeys) {
            if (dayKey == null || dayKey.isBlank()) {
                continue;
            }
            normalized.add(dayKey.trim().toLowerCase());
        }
        return normalized;
    }

    private String normalizeTurnName(String shiftName) {
        return shiftName == null ? "" : shiftName.trim().toUpperCase();
    }

    private int resolveSortOrder(String turnName) {
        if (turnName == null) {
            return 99;
        }
        return switch (turnName.toUpperCase()) {
            case "1ER" -> 1;
            case "2DO" -> 2;
            case "3ER" -> 3;
            case "4A", "4TO" -> 4;
            case "4B" -> 5;
            case "TE" -> 90;
            case "TE1" -> 91;
            case "TE2" -> 92;
            case "TE3" -> 93;
            case "TE4" -> 94;
            default -> 99;
        };
    }
}
