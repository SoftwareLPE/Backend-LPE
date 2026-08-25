package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.plant.PlantDTO;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;

import java.util.List;

public class CompanyMapper {
    public static CompanyListDTO toListDTO(Company c) {
        return new CompanyListDTO(c.getCompanyId(), c.getCompanyName());
    }

    public static PlantDTO toPlantDTO(Plant p) {
        return new PlantDTO(
                p.getPlantId(),
                p.getPlantName(),
                p.getFormatCatalogId(),
                p.getFormatTypeId(),
                p.getActive()
        );
    }

    public static CompanyDetailDTO toDetailDTO(Company c) {
        List<PlantDTO> plants = (c.getPlants() == null) ? List.of()
                : c.getPlants().stream().map(CompanyMapper::toPlantDTO).toList();

        return new CompanyDetailDTO(c.getCompanyId(), c.getCompanyName(), plants);
    }
}
