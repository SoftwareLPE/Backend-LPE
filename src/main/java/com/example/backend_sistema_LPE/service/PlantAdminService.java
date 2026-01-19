package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.UpdatePlantNameDTO;

public interface PlantAdminService {

    UpdatePlantNameDTO updatePlantName(Long plantId, Long companyId, UpdatePlantNameDTO updatePlantNameDTO);

    void deletePlant(Long companyId, Long plantId);
}
