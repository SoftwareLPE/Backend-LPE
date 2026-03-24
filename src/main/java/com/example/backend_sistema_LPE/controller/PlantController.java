package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.PlantFormatDTO;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/plants")
public class PlantController {
    private final PlantService plantService;


    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping("/{plantId}/format")
    public ResponseEntity<PlantFormatDTO> getPlantFormat(@PathVariable Long plantId) {
        return plantService.getByPlantId(plantId)
                .map(plant -> ResponseEntity.ok(
                        new PlantFormatDTO(
                                plant.getPlantId(),
                                plant.getFormatCatalogId(),
                                plant.getFormatTypeId()
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }
}

