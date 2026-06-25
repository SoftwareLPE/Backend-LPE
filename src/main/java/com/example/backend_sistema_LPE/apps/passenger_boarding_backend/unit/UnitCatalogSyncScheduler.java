package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UnitCatalogSyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(UnitCatalogSyncScheduler.class);

    private final PlantRepository plantRepository;
    private final UnitCatalogSyncService unitCatalogSyncService;

    public UnitCatalogSyncScheduler(
            PlantRepository plantRepository,
            UnitCatalogSyncService unitCatalogSyncService
    ) {
        this.plantRepository = plantRepository;
        this.unitCatalogSyncService = unitCatalogSyncService;
    }

    @Scheduled(cron = "0 0 10,20 * * *", zone = "America/Ojinaga")
    public void syncUnitsCatalog() {
        List<Long> plantIds = plantRepository.findPlantIdsWithWialonUnitsGroupId();
        if (plantIds.isEmpty()) {
            log.info("Unit catalog scheduled sync skipped: no plants configured with wialonUnitsGroupId.");
            return;
        }

        log.info("Starting scheduled unit catalog sync for {} plants.", plantIds.size());

        for (Long plantId : plantIds) {
            try {
                UnitCatalogSyncResponseDTO result = unitCatalogSyncService.syncPlantUnits(plantId);
                log.info(
                        "Scheduled unit sync completed for plantId={} groupId={} found={} created={} updated={} reactivated={} inactivated={}",
                        result.getPlantId(),
                        result.getWialonUnitsGroupId(),
                        result.getWialonUnitsCount(),
                        result.getCreatedUnitsCount(),
                        result.getUpdatedUnitsCount(),
                        result.getReactivatedUnitsCount(),
                        result.getInactivatedUnitsCount()
                );
            } catch (Exception ex) {
                log.error("Scheduled unit sync failed for plantId={}: {}", plantId, ex.getMessage(), ex);
            }
        }

        log.info("Scheduled unit catalog sync finished.");
    }
}
