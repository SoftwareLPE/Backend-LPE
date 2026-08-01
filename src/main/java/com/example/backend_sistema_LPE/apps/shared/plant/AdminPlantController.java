package com.example.backend_sistema_LPE.apps.shared.plant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminPlantController {
    private final PlantAdminService plantAdminService;

    public AdminPlantController(PlantAdminService plantAdminService) {
        this.plantAdminService = plantAdminService;
    }

    @GetMapping("/plants/catalog")
    public ResponseEntity<List<PlantCatalogRowDTO>> getPlantCatalog(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(plantAdminService.getPlantCatalog(companyId, search));
    }

    @PatchMapping("/companies/{companyId}/plants/{plantId}")
    public ResponseEntity<UpdatePlantNameDTO> updatePlantName(
            @PathVariable Long companyId,
            @PathVariable Long plantId,
            @RequestBody UpdatePlantNameDTO updatePlantNameDTO
    ) {
        return ResponseEntity.ok(plantAdminService.updatePlantName(plantId, companyId, updatePlantNameDTO));
    }

    @PatchMapping("/companies/{companyId}/plants/{plantId}/status")
    public ResponseEntity<PlantStatusDTO> updatePlantStatus(
            @PathVariable Long companyId,
            @PathVariable Long plantId,
            @RequestBody UpdatePlantActiveDTO request
    ) {
        return ResponseEntity.ok(plantAdminService.updatePlantActive(plantId, companyId, request.getActive()));
    }

    @DeleteMapping("/companies/{companyId}/plants/{plantId}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long companyId,
            @PathVariable Long plantId
    ) {
        plantAdminService.deletePlant(companyId, plantId);
        return ResponseEntity.noContent().build();
    }
}
