package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.UpdatePlantNameDTO;
import com.example.backend_sistema_LPE.model.FormatCatalog;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.FormatCatalogRepository;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
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
    private final FormatCatalogRepository formatCatalogRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final ShiftRepository shiftRepository;

    public PlantAdminServiceImpl(
            PlantRepository plantRepository,
            UserPlantRepository userPlantRepository,
            RolePlantRepository rolePlantRepository,
            DriverRepository driverRepository,
            DriverRouteRepository driverRouteRepository,
            FormatCatalogRepository formatCatalogRepository,
            FormatTypeRepository formatTypeRepository,
            ShiftRepository shiftRepository
    ) {
        this.plantRepository = plantRepository;
        this.userPlantRepository = userPlantRepository;
        this.rolePlantRepository = rolePlantRepository;
        this.driverRepository = driverRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.formatCatalogRepository = formatCatalogRepository;
        this.formatTypeRepository = formatTypeRepository;
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
        if (updatePlantNameDTO.getFormatCatalogId() != null || updatePlantNameDTO.getFormatTypeId() != null) {
            plant.setFormatTypeId(resolveFormatTypeId(
                    plant.getFormatCatalogId(),
                    updatePlantNameDTO.getFormatTypeId() != null
                            ? updatePlantNameDTO.getFormatTypeId()
                            : plant.getFormatTypeId()
            ));
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

    private Long resolveFormatTypeId(Long formatCatalogId, Long requestedFormatTypeId) {
        if (formatCatalogId == null) {
            throw new RuntimeException("formatCatalogId is required");
        }

        FormatCatalog formatCatalog = formatCatalogRepository.findById(formatCatalogId)
                .orElseThrow(() -> new RuntimeException("Format catalog not found " + formatCatalogId));

        if (!"CUSTOM".equalsIgnoreCase(formatCatalog.getFormatCategory())) {
            return null;
        }

        FormatType linkedFormatType = formatTypeRepository.findByFormatCatalogId(formatCatalogId)
                .orElse(null);

        if (linkedFormatType == null) {
            if (requestedFormatTypeId == null) {
                throw new RuntimeException("No formatType linked to formatCatalogId: " + formatCatalogId);
            }
            return formatTypeRepository.findById(requestedFormatTypeId)
                    .orElseThrow(() -> new RuntimeException("Format type not found " + requestedFormatTypeId))
                    .getFormatTypeId();
        }

        if (requestedFormatTypeId != null && !requestedFormatTypeId.equals(linkedFormatType.getFormatTypeId())) {
            throw new RuntimeException(
                    "formatTypeId does not match formatCatalogId. Expected "
                            + linkedFormatType.getFormatTypeId()
                            + " for formatCatalogId "
                            + formatCatalogId
            );
        }

        return linkedFormatType.getFormatTypeId();
    }

}
