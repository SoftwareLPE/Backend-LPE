package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.UpdatePlantNameDTO;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RolePlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.UserPlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PlantAdminServiceImpl implements PlantAdminService{
    private final PlantRepository plantRepository;
    private final UserPlantRepository userPlantRepository;
    private final RolePlantRepository rolePlantRepository;
    private final DriverRepository driverRepository;
    private final DriverRouteRepository driverRouteRepository;
    private final ShiftRepository shiftRepository;

    public PlantAdminServiceImpl(
            PlantRepository plantRepository,
            UserPlantRepository userPlantRepository,
            RolePlantRepository rolePlantRepository,
            DriverRepository driverRepository,
            DriverRouteRepository driverRouteRepository,
            ShiftRepository shiftRepository
    ) {
        this.plantRepository = plantRepository;
        this.userPlantRepository = userPlantRepository;
        this.rolePlantRepository = rolePlantRepository;
        this.driverRepository = driverRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.shiftRepository = shiftRepository;
    }

    //Endpoint para actualizar nombre de plantas
    @Override
    @Transactional
    public UpdatePlantNameDTO updatePlantName(Long plantId,Long companyId, UpdatePlantNameDTO updatePlantNameDTO) {
        Plant plant = plantRepository.findByPlantIdAndCompanyCompanyId(plantId,companyId)
                .orElseThrow(()->new RuntimeException("Plant not found in company. plantId=" + plantId + " companyId=" + companyId));

        if (updatePlantNameDTO.getPlantName() != null && !updatePlantNameDTO.getPlantName().trim().isBlank()) {
            plant.setPlantName(updatePlantNameDTO.getPlantName().trim());
        }
        if (updatePlantNameDTO.getFormatCatalogId() != null) {
            plant.setFormatCatalogId(updatePlantNameDTO.getFormatCatalogId());
        }
        if (updatePlantNameDTO.getFormatTypeId() != null) {
            plant.setFormatTypeId(updatePlantNameDTO.getFormatTypeId());
        }

        plantRepository.save(plant);

        return new UpdatePlantNameDTO(
                plant.getPlantName(),
                plant.getFormatCatalogId(),
                plant.getFormatTypeId()
        );
    }

    @Override
    @Transactional
    public void deletePlant(Long companyId, Long plantId) {
        Plant plant = plantRepository.findByPlantIdAndCompanyCompanyId(plantId, companyId)
                .orElseThrow(() -> new RuntimeException("Plant not found in company. plantId=" + plantId + " companyId=" + companyId));

        shiftRepository.deleteByPlantPlantId(plantId);
        driverRouteRepository.deleteByDriverPlantPlantId(plantId);
        driverRepository.deleteByPlantPlantId(plantId);

        rolePlantRepository.deleteByPlantPlantId(plantId);
        userPlantRepository.deleteByPlantPlantId(plantId);

        plantRepository.delete(plant);
    }

}
