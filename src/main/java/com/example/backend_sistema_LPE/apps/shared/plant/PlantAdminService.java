package com.example.backend_sistema_LPE.apps.shared.plant;

public interface PlantAdminService {

    java.util.List<PlantCatalogRowDTO> getPlantCatalog(Long companyId, String search);

    UpdatePlantNameDTO updatePlantName(Long plantId, Long companyId, UpdatePlantNameDTO updatePlantNameDTO);

    PlantStatusDTO updatePlantActive(Long plantId, Long companyId, Boolean active);

    void deletePlant(Long companyId, Long plantId);
}
