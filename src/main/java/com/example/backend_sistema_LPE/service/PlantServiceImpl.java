package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
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
