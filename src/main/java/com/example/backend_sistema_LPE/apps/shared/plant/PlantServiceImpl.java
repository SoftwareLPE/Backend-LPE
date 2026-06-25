package com.example.backend_sistema_LPE.apps.shared.plant;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlantServiceImpl implements PlantService {
    private final PlantRepository plantRepository;

    public PlantServiceImpl(PlantRepository plantRepository) {
        this.plantRepository = plantRepository;
    }


    @Override
    public Optional<Plant> getByPlantId(Long plantId) {
        return plantRepository.findById(plantId);

    }
}
