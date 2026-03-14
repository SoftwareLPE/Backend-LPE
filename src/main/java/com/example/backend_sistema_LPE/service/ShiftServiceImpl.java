package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateShiftRequestDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateShiftRequestDTO;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.ShiftFormatTurnMap;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.ShiftFormatTurnMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final PlantRepository plantRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final FormatTurnConfigRepository formatTurnConfigRepository;
    private final ShiftFormatTurnMapRepository shiftFormatTurnMapRepository;

    public ShiftServiceImpl(
            ShiftRepository shiftRepository,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            FormatTurnConfigRepository formatTurnConfigRepository,
            ShiftFormatTurnMapRepository shiftFormatTurnMapRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.formatTurnConfigRepository = formatTurnConfigRepository;
        this.shiftFormatTurnMapRepository = shiftFormatTurnMapRepository;
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

        Shift existingShift = findExistingShiftByNormalizedName(plantId, request.getShiftName());
        if (existingShift != null) {
            String oldShiftName = existingShift.getShiftName();
            Set<String> oldDayKeys = existingShift.getDayKeys() == null
                    ? Set.of()
                    : new HashSet<>(existingShift.getDayKeys());

            existingShift.setShiftName(request.getShiftName().trim());
            existingShift.setStartTime(request.getStartTime());
            existingShift.setEndTime(request.getEndTime());
            existingShift.setDayKeys(request.getDayKeys());

            Shift savedExisting = shiftRepository.save(existingShift);
            syncTurnConfigForUpdate(
                    plant,
                    oldShiftName,
                    oldDayKeys,
                    savedExisting.getShiftName(),
                    savedExisting.getDayKeys()
            );
            syncShiftMappingsForShift(plant, savedExisting);
            return toDTO(savedExisting);
        }

        Shift shift = new Shift();
        shift.setPlant(plant);
        shift.setShiftName(request.getShiftName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setDayKeys(request.getDayKeys());

        Shift saved = shiftRepository.save(shift);
        syncTurnConfigForShift(plant, saved.getShiftName(), saved.getDayKeys());
        syncShiftMappingsForShift(plant, saved);
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
        syncShiftMappingsForShift(saved.getPlant(), saved);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteShift(Long plantId, Long shiftId) {
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));
        deleteShiftMappings(shift.getPlant(), shift.getShiftId());
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

    private void syncShiftMappingsForShift(Plant plant, Shift shift) {
        FormatType formatType = resolveFormatType(plant);
        if (formatType == null || shift == null || shift.getShiftId() == null) {
            return;
        }

        String normalizedName = normalizeTurnName(shift.getShiftName());
        Set<String> normalizedDays = normalizeDayKeys(shift.getDayKeys());
        List<ShiftFormatTurnMap> existingMappings = shiftFormatTurnMapRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeIdAndShiftShiftId(
                        plant.getPlantId(),
                        formatType.getFormatTypeId(),
                        shift.getShiftId()
                );
        Map<String, ShiftFormatTurnMap> existingByDay = new HashMap<>();
        for (ShiftFormatTurnMap existing : existingMappings) {
            existingByDay.put(existing.getDayOfWeek(), existing);
        }
        if (normalizedDays.isEmpty()) {
            if (!existingMappings.isEmpty()) {
                shiftFormatTurnMapRepository.deleteAll(existingMappings);
            }
            return;
        }

        List<ShiftFormatTurnMap> mappings = new ArrayList<>();
        for (String dayKey : normalizedDays) {
            FormatTurnConfig config = formatTurnConfigRepository
                    .findByFormatTypeFormatTypeIdAndDayOfWeekAndTurnName(
                            formatType.getFormatTypeId(),
                            dayKey,
                            normalizedName
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "No turnConfig mapping found for shift name: " + normalizedName
                    ));

            ShiftFormatTurnMap map = existingByDay.remove(config.getDayOfWeek());
            if (map == null) {
                map = new ShiftFormatTurnMap();
                map.setPlant(plant);
                map.setFormatType(formatType);
                map.setShift(shift);
            }
            map.setDayOfWeek(config.getDayOfWeek());
            map.setTurnName(config.getTurnName());
            mappings.add(map);
        }

        if (!existingByDay.isEmpty()) {
            shiftFormatTurnMapRepository.deleteAll(existingByDay.values());
        }

        if (!mappings.isEmpty()) {
            shiftFormatTurnMapRepository.saveAll(mappings);
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

    private void deleteShiftMappings(Plant plant, Long shiftId) {
        FormatType formatType = resolveFormatType(plant);
        if (formatType == null || plant == null || shiftId == null) {
            return;
        }
        shiftFormatTurnMapRepository.deleteByPlantPlantIdAndFormatTypeFormatTypeIdAndShiftShiftId(
                plant.getPlantId(),
                formatType.getFormatTypeId(),
                shiftId
        );
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
        if (shiftName == null) {
            return "";
        }
        String normalized = Normalizer.normalize(shiftName.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("\\s+", " ")
                .trim();
        return normalized.toUpperCase();
    }

    private Shift findExistingShiftByNormalizedName(Long plantId, String shiftName) {
        String normalizedName = normalizeTurnName(shiftName);
        return shiftRepository.findByPlantPlantId(plantId).stream()
                .filter(shift -> normalizeTurnName(shift.getShiftName()).equals(normalizedName))
                .findFirst()
                .orElse(null);
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
