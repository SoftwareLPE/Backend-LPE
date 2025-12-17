package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Plant;

import java.util.List;
import java.util.Optional;

public interface PlantService {

    Optional<Plant> getByPlantId(Long plantId);
}
