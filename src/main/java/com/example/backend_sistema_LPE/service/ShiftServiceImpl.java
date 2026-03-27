package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateShiftRequestDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateShiftRequestDTO;
import com.example.backend_sistema_LPE.enums.ShiftType;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.FormatTypeTeRule;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.ShiftFormatTurnMap;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import com.example.backend_sistema_LPE.repository.FormatTypeTeRuleRepository;
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
    private final FormatTypeTeRuleRepository formatTypeTeRuleRepository;
    private final ShiftFormatTurnMapRepository shiftFormatTurnMapRepository;

    public ShiftServiceImpl(
            ShiftRepository shiftRepository,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            FormatTurnConfigRepository formatTurnConfigRepository,
            FormatTypeTeRuleRepository formatTypeTeRuleRepository,
            ShiftFormatTurnMapRepository shiftFormatTurnMapRepository
    ) {
        this.shiftRepository = shiftRepository;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.formatTurnConfigRepository = formatTurnConfigRepository;
        this.formatTypeTeRuleRepository = formatTypeTeRuleRepository;
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
        Plant plant = lockPlant(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found " + plantId));

        Shift existingShift = findExistingShiftByNormalizedName(plantId, request.getShiftName());
        if (existingShift != null) {
            String oldShiftName = existingShift.getShiftName();
            Set<String> oldDayKeys = existingShift.getDayKeys() == null
                    ? Set.of()
                    : new HashSet<>(existingShift.getDayKeys());
            ShiftType oldShiftType = effectiveShiftType(existingShift);
            Set<String> oldLongWeekDayKeys = existingShift.getLongWeekDayKeys() == null
                    ? Set.of()
                    : new HashSet<>(existingShift.getLongWeekDayKeys());
            Set<String> oldShortWeekDayKeys = existingShift.getShortWeekDayKeys() == null
                    ? Set.of()
                    : new HashSet<>(existingShift.getShortWeekDayKeys());
            ShiftType requestedShiftType = request.getShiftType() == null
                    ? effectiveShiftType(existingShift)
                    : request.getShiftType();
            Set<String> requestedDayKeys = request.getDayKeys() == null
                    ? oldDayKeys
                    : request.getDayKeys();
            Set<String> requestedLongWeekDayKeys = request.getLongWeekDayKeys() == null
                    ? oldLongWeekDayKeys
                    : request.getLongWeekDayKeys();
            Set<String> requestedShortWeekDayKeys = request.getShortWeekDayKeys() == null
                    ? oldShortWeekDayKeys
                    : request.getShortWeekDayKeys();

            existingShift.setShiftName(request.getShiftName().trim());
            existingShift.setStartTime(request.getStartTime());
            existingShift.setEndTime(request.getEndTime());
            applyShiftSchedule(existingShift, resolveShiftType(requestedShiftType), requestedDayKeys, requestedLongWeekDayKeys, requestedShortWeekDayKeys);

            Shift savedExisting = shiftRepository.save(existingShift);
            syncTurnConfigForUpdate(
                    plant,
                    oldShiftName,
                    oldDayKeys,
                    oldShiftType,
                    oldLongWeekDayKeys,
                    oldShortWeekDayKeys,
                    savedExisting.getShiftName(),
                    savedExisting.getDayKeys(),
                    effectiveShiftType(savedExisting),
                    savedExisting.getLongWeekDayKeys(),
                    savedExisting.getShortWeekDayKeys()
            );
            syncShiftMappingsForShift(plant, savedExisting);
            return toDTO(savedExisting);
        }

        Shift shift = new Shift();
        shift.setPlant(plant);
        shift.setShiftName(request.getShiftName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        applyShiftSchedule(shift, resolveShiftType(request.getShiftType()), request.getDayKeys(), request.getLongWeekDayKeys(), request.getShortWeekDayKeys());

        Shift saved = shiftRepository.save(shift);
        syncTurnConfigForShift(plant, saved.getShiftName(), effectiveDayKeys(saved));
        syncShiftMappingsForShift(plant, saved);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public ShiftDTO updateShift(Long plantId, Long shiftId, UpdateShiftRequestDTO request) {
        Plant plant = lockPlant(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found " + plantId));
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));

        String oldShiftName = shift.getShiftName();
        java.util.Set<String> oldDayKeys = shift.getDayKeys() == null
                ? java.util.Set.of()
                : new java.util.HashSet<>(shift.getDayKeys());
        ShiftType oldShiftType = effectiveShiftType(shift);
        java.util.Set<String> oldLongWeekDayKeys = shift.getLongWeekDayKeys() == null
                ? java.util.Set.of()
                : new java.util.HashSet<>(shift.getLongWeekDayKeys());
        java.util.Set<String> oldShortWeekDayKeys = shift.getShortWeekDayKeys() == null
                ? java.util.Set.of()
                : new java.util.HashSet<>(shift.getShortWeekDayKeys());

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
        if (request.getShiftType() != null) {
            shift.setShiftType(request.getShiftType());
            hasUpdates = true;
        }
        if (request.getLongWeekDayKeys() != null) {
            shift.setLongWeekDayKeys(request.getLongWeekDayKeys());
            hasUpdates = true;
        }
        if (request.getShortWeekDayKeys() != null) {
            shift.setShortWeekDayKeys(request.getShortWeekDayKeys());
            hasUpdates = true;
        }

        if (!hasUpdates) {
            return toDTO(shift);
        }

        validateShiftSchedule(shift.getShiftName(), effectiveShiftType(shift), shift.getDayKeys(), shift.getLongWeekDayKeys(), shift.getShortWeekDayKeys());
        normalizeShiftSchedule(shift, effectiveShiftType(shift));

        Shift saved = shiftRepository.save(shift);
        syncTurnConfigForUpdate(
                plant,
                oldShiftName,
                oldDayKeys,
                oldShiftType,
                oldLongWeekDayKeys,
                oldShortWeekDayKeys,
                saved.getShiftName(),
                saved.getDayKeys(),
                effectiveShiftType(saved),
                saved.getLongWeekDayKeys(),
                saved.getShortWeekDayKeys()
        );
        syncShiftMappingsForShift(plant, saved);
        return toDTO(saved);
    }

    @Override
    @Transactional
    public void deleteShift(Long plantId, Long shiftId) {
        Plant plant = lockPlant(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found " + plantId));
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));
        deleteShiftMappings(plant, shift.getShiftId());
        deleteTurnConfigForShift(plant, shift.getShiftName(), effectiveDayKeys(shift));
        shiftRepository.delete(shift);
    }

    private ShiftDTO toDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setShiftId(shift.getShiftId());
        dto.setShiftName(shift.getShiftName());
        dto.setDayKeys(shift.getDayKeys());
        dto.setShiftType(effectiveShiftType(shift));
        dto.setLongWeekDayKeys(shift.getLongWeekDayKeys());
        dto.setShortWeekDayKeys(shift.getShortWeekDayKeys());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        return dto;
    }

    private void validateRequest(CreateShiftRequestDTO request) {
        validateShiftSchedule(
                request.getShiftName(),
                resolveShiftType(request.getShiftType()),
                request.getDayKeys(),
                request.getLongWeekDayKeys(),
                request.getShortWeekDayKeys()
        );
    }

    private void validateRequest(UpdateShiftRequestDTO request) {
    }

    private void syncTurnConfigForUpdate(
            Plant plant,
            String oldShiftName,
            java.util.Set<String> oldDayKeys,
            ShiftType oldShiftType,
            java.util.Set<String> oldLongWeekDayKeys,
            java.util.Set<String> oldShortWeekDayKeys,
            String newShiftName,
            java.util.Set<String> newDayKeys,
            ShiftType newShiftType,
            java.util.Set<String> newLongWeekDayKeys,
            java.util.Set<String> newShortWeekDayKeys
    ) {
        String normalizedOldName = normalizeTurnName(oldShiftName);
        String normalizedNewName = normalizeTurnName(newShiftName);
        java.util.Set<String> oldDays = effectiveDayKeys(oldShiftType, oldDayKeys, oldLongWeekDayKeys, oldShortWeekDayKeys);
        java.util.Set<String> newDays = effectiveDayKeys(newShiftType, newDayKeys, newLongWeekDayKeys, newShortWeekDayKeys);

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

        syncTeTurnConfigs(formatType, normalizedDays);
    }

    private void syncShiftMappingsForShift(Plant plant, Shift shift) {
        FormatType formatType = resolveFormatType(plant);
        if (formatType == null || shift == null || shift.getShiftId() == null) {
            return;
        }

        String normalizedName = normalizeTurnName(shift.getShiftName());
        Set<String> normalizedDays = effectiveDayKeys(shift);
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

        syncTeTurnConfigs(formatType, normalizedDays);
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

    private java.util.Optional<Plant> lockPlant(Long plantId) {
        return plantRepository.findByIdForUpdate(plantId);
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

    private ShiftType resolveShiftType(ShiftType shiftType) {
        return shiftType == null ? ShiftType.REGULAR : shiftType;
    }

    private ShiftType effectiveShiftType(Shift shift) {
        return shift == null || shift.getShiftType() == null ? ShiftType.REGULAR : shift.getShiftType();
    }

    private java.util.Set<String> effectiveDayKeys(Shift shift) {
        if (shift == null) {
            return java.util.Set.of();
        }
        return effectiveDayKeys(
                effectiveShiftType(shift),
                shift.getDayKeys(),
                shift.getLongWeekDayKeys(),
                shift.getShortWeekDayKeys()
        );
    }

    private java.util.Set<String> effectiveDayKeys(
            ShiftType shiftType,
            java.util.Set<String> dayKeys,
            java.util.Set<String> longWeekDayKeys,
            java.util.Set<String> shortWeekDayKeys
    ) {
        if (shiftType == ShiftType.SPECIAL) {
            java.util.Set<String> union = new java.util.HashSet<>(normalizeDayKeys(longWeekDayKeys));
            union.addAll(normalizeDayKeys(shortWeekDayKeys));
            return union;
        }
        return normalizeDayKeys(dayKeys);
    }

    private void applyShiftSchedule(
            Shift shift,
            ShiftType shiftType,
            java.util.Set<String> dayKeys,
            java.util.Set<String> longWeekDayKeys,
            java.util.Set<String> shortWeekDayKeys
    ) {
        validateShiftSchedule(shift.getShiftName(), shiftType, dayKeys, longWeekDayKeys, shortWeekDayKeys);
        shift.setShiftType(shiftType);
        shift.setDayKeys(normalizeDayKeys(dayKeys));
        shift.setLongWeekDayKeys(normalizeDayKeys(longWeekDayKeys));
        shift.setShortWeekDayKeys(normalizeDayKeys(shortWeekDayKeys));
        normalizeShiftSchedule(shift, shiftType);
    }

    private void normalizeShiftSchedule(Shift shift, ShiftType shiftType) {
        if (shiftType == ShiftType.SPECIAL) {
            shift.setDayKeys(new java.util.HashSet<>());
            return;
        }
        shift.setLongWeekDayKeys(new java.util.HashSet<>());
        shift.setShortWeekDayKeys(new java.util.HashSet<>());
    }

    private void validateShiftSchedule(
            String shiftName,
            ShiftType shiftType,
            java.util.Set<String> dayKeys,
            java.util.Set<String> longWeekDayKeys,
            java.util.Set<String> shortWeekDayKeys
    ) {
        if (shiftName == null || shiftName.trim().isBlank()) {
            throw new RuntimeException("shiftName is required");
        }
        if (shiftType == ShiftType.SPECIAL) {
            if (longWeekDayKeys == null || longWeekDayKeys.isEmpty()) {
                throw new RuntimeException("longWeekDayKeys is required for SPECIAL shift");
            }
            if (shortWeekDayKeys == null || shortWeekDayKeys.isEmpty()) {
                throw new RuntimeException("shortWeekDayKeys is required for SPECIAL shift");
            }
            return;
        }
        if (dayKeys == null || dayKeys.isEmpty()) {
            throw new RuntimeException("dayKeys is required");
        }
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

    private void syncTeTurnConfigs(FormatType formatType, java.util.Set<String> dayKeys) {
        if (formatType == null || dayKeys == null || dayKeys.isEmpty()) {
            return;
        }

        for (String dayKey : dayKeys) {
            syncTeTurnConfigsForDay(formatType, dayKey);
        }
    }

    private void syncTeTurnConfigsForDay(FormatType formatType, String dayKey) {
        if (formatType == null || dayKey == null || dayKey.isBlank()) {
            return;
        }

        String normalizedDayKey = dayKey.trim().toLowerCase();
        List<FormatTurnConfig> dayConfigs = formatTurnConfigRepository
                .findByFormatTypeFormatTypeIdAndDayOfWeek(formatType.getFormatTypeId(), normalizedDayKey);

        List<FormatTurnConfig> normalConfigs = dayConfigs.stream()
                .filter(config -> !isTeTurnName(config.getTurnName()))
                .toList();

        List<FormatTurnConfig> teConfigs = dayConfigs.stream()
                .filter(config -> isTeTurnName(config.getTurnName()))
                .toList();

        java.util.Optional<FormatTypeTeRule> teRule = formatTypeTeRuleRepository
                .findByFormatTypeFormatTypeIdAndDayOfWeek(formatType.getFormatTypeId(), normalizedDayKey);

        if (teRule.isEmpty()) {
            return;
        }

        int desiredTeCount = Boolean.TRUE.equals(teRule.get().getActive())
                ? Math.max(teRule.get().getTeCount(), 0)
                : 0;

        if (normalConfigs.isEmpty() || desiredTeCount == 0) {
            if (!teConfigs.isEmpty()) {
                formatTurnConfigRepository.deleteAll(teConfigs);
            }
            return;
        }

        Map<String, FormatTurnConfig> existingTeByName = new HashMap<>();
        List<FormatTurnConfig> duplicateTeConfigs = new ArrayList<>();
        for (FormatTurnConfig teConfig : teConfigs) {
            String normalizedTeName = normalizeTurnName(teConfig.getTurnName());
            FormatTurnConfig previous = existingTeByName.putIfAbsent(normalizedTeName, teConfig);
            if (previous != null) {
                duplicateTeConfigs.add(teConfig);
            }
        }

        Set<String> desiredTeNames = new HashSet<>();
        int nextSortOrder = normalConfigs.stream()
                .map(FormatTurnConfig::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<FormatTurnConfig> toSave = new ArrayList<>();
        for (int index = 1; index <= desiredTeCount; index++) {
            String desiredTeName = "TE" + index;
            desiredTeNames.add(desiredTeName);

            FormatTurnConfig teConfig = existingTeByName.get(desiredTeName);
            if (teConfig == null) {
                teConfig = new FormatTurnConfig();
                teConfig.setFormatType(formatType);
                teConfig.setDayOfWeek(normalizedDayKey);
                teConfig.setTurnName(desiredTeName);
            }

            teConfig.setSortOrder(++nextSortOrder);
            toSave.add(teConfig);
        }

        List<FormatTurnConfig> toDelete = teConfigs.stream()
                .filter(config -> !desiredTeNames.contains(normalizeTurnName(config.getTurnName())))
                .toList();

        if (!duplicateTeConfigs.isEmpty()) {
            List<FormatTurnConfig> mergedDeleteList = new ArrayList<>(toDelete);
            mergedDeleteList.addAll(duplicateTeConfigs);
            formatTurnConfigRepository.deleteAll(mergedDeleteList);
        } else if (!toDelete.isEmpty()) {
            formatTurnConfigRepository.deleteAll(toDelete);
        }

        if (!toSave.isEmpty()) {
            formatTurnConfigRepository.saveAll(toSave);
        }
    }

    private boolean isTeTurnName(String turnName) {
        String normalized = normalizeTurnName(turnName);
        return normalized.startsWith("TE");
    }
}
