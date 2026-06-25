package com.example.backend_sistema_LPE.apps.shared.plant;

public interface PlantAdminService {

    UpdatePlantNameDTO updatePlantName(Long plantId, Long companyId, UpdatePlantNameDTO updatePlantNameDTO);

    void deletePlant(Long companyId, Long plantId);
}
