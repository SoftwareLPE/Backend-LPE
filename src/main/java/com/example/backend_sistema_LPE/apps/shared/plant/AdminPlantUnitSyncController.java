package com.example.backend_sistema_LPE.apps.shared.plant;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitCatalogSyncResponseDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitCatalogSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/plants")
public class AdminPlantUnitSyncController {
    private final UnitCatalogSyncService unitCatalogSyncService;

    public AdminPlantUnitSyncController(UnitCatalogSyncService unitCatalogSyncService) {
        this.unitCatalogSyncService = unitCatalogSyncService;
    }

    @PostMapping("/{plantId}/sync-units")
    public ResponseEntity<UnitCatalogSyncResponseDTO> syncUnits(@PathVariable Long plantId) {
        return ResponseEntity.ok(unitCatalogSyncService.syncPlantUnits(plantId));
    }
}
