package com.example.backend_sistema_LPE.apps.shared.plant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/companies/{companyId}/plants")
public class AdminPlantController {
    private final PlantAdminService plantAdminService;

    public AdminPlantController(PlantAdminService plantAdminService) {
        this.plantAdminService = plantAdminService;
    }

    @PatchMapping("/{plantId}")
    public ResponseEntity<UpdatePlantNameDTO> updatePlantName(
            @PathVariable Long companyId,
            @PathVariable Long plantId,
            @RequestBody UpdatePlantNameDTO updatePlantNameDTO
    ) {
        return ResponseEntity.ok(plantAdminService.updatePlantName(plantId, companyId, updatePlantNameDTO));
    }

    @DeleteMapping("/{plantId}")
    public ResponseEntity<Void> deletePlant(
            @PathVariable Long companyId,
            @PathVariable Long plantId
    ) {
        plantAdminService.deletePlant(companyId, plantId);
        return ResponseEntity.noContent().build();
    }
}
