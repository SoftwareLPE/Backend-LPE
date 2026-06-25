package com.example.backend_sistema_LPE.apps.shared.plant;

import java.util.Optional;

public interface PlantService {

    Optional<Plant> getByPlantId(Long plantId);
}
