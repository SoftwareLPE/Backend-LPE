package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.service.PlantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Entity;

@RestController
@RequestMapping("/plants")
@CrossOrigin(origins = "http://localhost:8081/")
public class PlantController {
    private final PlantService plantService;


    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    @GetMapping
    public ResponseEntity<Plant> getCompanyById(@PathVariable Long plantId) {
        return plantService.getByPlantId(plantId)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());


    }
}

