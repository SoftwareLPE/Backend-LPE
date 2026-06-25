package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class UnitCatalogSyncServiceImpl implements UnitCatalogSyncService {
    private final PlantRepository plantRepository;
    private final UnitRepository unitRepository;
    private final WialonUnitGroupClient wialonUnitGroupClient;
    private final WialonUnitClient wialonUnitClient;
    private final UnitNameNormalizationService unitNameNormalizationService;

    public UnitCatalogSyncServiceImpl(
            PlantRepository plantRepository,
            UnitRepository unitRepository,
            WialonUnitGroupClient wialonUnitGroupClient,
            WialonUnitClient wialonUnitClient,
            UnitNameNormalizationService unitNameNormalizationService
    ) {
        this.plantRepository = plantRepository;
        this.unitRepository = unitRepository;
        this.wialonUnitGroupClient = wialonUnitGroupClient;
        this.wialonUnitClient = wialonUnitClient;
        this.unitNameNormalizationService = unitNameNormalizationService;
    }

    @Override
    public UnitCatalogSyncResponseDTO syncPlantUnits(Long plantId) {
        Plant plant = plantRepository.findByIdForUpdate(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found: " + plantId));

        if (plant.getWialonUnitsGroupId() == null) {
            throw new RuntimeException("Plant does not have wialonUnitsGroupId configured: " + plantId);
        }

        JsonNode searchResponse = wialonUnitGroupClient.searchUnitGroups();
        JsonNode groupNode = findGroupById(searchResponse, plant.getWialonUnitsGroupId());
        if (groupNode == null) {
            throw new RuntimeException(
                    "Wialon units group not found for plantId=" + plantId +
                            " wialonUnitsGroupId=" + plant.getWialonUnitsGroupId()
            );
        }

        String groupName = groupNode.path("nm").asText(null);
        Set<Long> wialonUnitIds = extractWialonUnitIds(groupNode.path("u"));
        Map<Long, WialonUnitItemDTO> remoteUnitsById = mapRemoteUnits(wialonUnitClient.searchUnitsByIds(wialonUnitIds));
        List<Unit> localUnits = unitRepository.findAllByPlantPlantId(plantId);
        Map<Long, Unit> localUnitsByWialonId = mapLocalUnitsByWialonId(localUnits);
        Timestamp now = Timestamp.from(Instant.now());

        int createdUnits = 0;
        int updatedUnits = 0;
        int activeUnits = 0;
        int reactivatedUnits = 0;
        int inactivatedUnits = 0;

        for (WialonUnitItemDTO remoteUnit : remoteUnitsById.values()) {
            Unit localUnit = localUnitsByWialonId.get(remoteUnit.getWialonId());
            if (localUnit == null) {
                Unit created = new Unit();
                created.setPlant(plant);
                created.setWialonId(remoteUnit.getWialonId());
                created.setNameRaw(resolveUnitName(remoteUnit.getUnitName(), remoteUnit.getWialonId(), null));
                unitNameNormalizationService.apply(created, created.getNameRaw());
                created.setActive(true);
                created.setLastSyncedAt(now);
                unitRepository.save(created);
                createdUnits++;
                activeUnits++;
                continue;
            }

            boolean wasActive = localUnit.isActive();
            if (!wasActive) {
                reactivatedUnits++;
            }

            String resolvedName = resolveUnitName(remoteUnit.getUnitName(), remoteUnit.getWialonId(), localUnit.getNameRaw());
            if (!Objects.equals(localUnit.getNameRaw(), resolvedName)) {
                localUnit.setNameRaw(resolvedName);
                unitNameNormalizationService.apply(localUnit, resolvedName);
                updatedUnits++;
            }

            localUnit.setActive(true);
            localUnit.setLastSyncedAt(now);
            activeUnits++;
        }

        for (Unit unit : localUnits) {
            boolean shouldBeActive = unit.getWialonId() != null && wialonUnitIds.contains(unit.getWialonId());
            if (shouldBeActive) {
                continue;
            }

            if (unit.isActive()) {
                unit.setActive(false);
                inactivatedUnits++;
            }
            unit.setLastSyncedAt(now);
        }

        plant.setLastSyncedAt(now);
        plantRepository.save(plant);

        return new UnitCatalogSyncResponseDTO(
                plant.getPlantId(),
                plant.getWialonUnitsGroupId(),
                groupName,
                wialonUnitIds.size(),
                createdUnits,
                updatedUnits,
                activeUnits,
                reactivatedUnits,
                inactivatedUnits,
                "Unit catalog synchronized successfully."
        );
    }

    private JsonNode findGroupById(JsonNode response, Long groupId) {
        JsonNode items = response.path("items");
        if (!items.isArray()) {
            return null;
        }
        for (JsonNode item : items) {
            if (item.path("id").asLong() == groupId) {
                return item;
            }
        }
        return null;
    }

    private Set<Long> extractWialonUnitIds(JsonNode unitsNode) {
        Set<Long> result = new HashSet<>();
        if (!unitsNode.isArray()) {
            return result;
        }
        for (JsonNode node : unitsNode) {
            if (node != null && node.isIntegralNumber()) {
                result.add(node.asLong());
            }
        }
        return result;
    }

    private Map<Long, WialonUnitItemDTO> mapRemoteUnits(List<WialonUnitItemDTO> remoteUnits) {
        Map<Long, WialonUnitItemDTO> result = new HashMap<>();
        for (WialonUnitItemDTO unit : remoteUnits) {
            if (unit.getWialonId() != null) {
                result.put(unit.getWialonId(), unit);
            }
        }
        return result;
    }

    private Map<Long, Unit> mapLocalUnitsByWialonId(List<Unit> localUnits) {
        Map<Long, Unit> result = new HashMap<>();
        for (Unit unit : localUnits) {
            if (unit.getWialonId() != null) {
                result.put(unit.getWialonId(), unit);
            }
        }
        return result;
    }

    private String resolveUnitName(String remoteName, Long wialonId, String currentName) {
        if (remoteName != null && !remoteName.isBlank()) {
            return remoteName.trim();
        }
        if (currentName != null && !currentName.isBlank()) {
            return currentName.trim();
        }
        return "UNIT-" + wialonId;
    }
}
