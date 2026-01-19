package com.example.backend_sistema_LPE.mapper;

import com.example.backend_sistema_LPE.dto.CompanyDetailDTO;
import com.example.backend_sistema_LPE.dto.CompanyListDTO;
import com.example.backend_sistema_LPE.dto.PlantDTO;
import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.model.Plant;

import java.util.List;

public class CompanyMapper {
    public static CompanyListDTO toListDTO(Company c) {
        return new CompanyListDTO(c.getCompanyId(), c.getCompanyName());
    }

    public static PlantDTO toPlantDTO(Plant p) {
        return new PlantDTO(p.getPlantId(), p.getPlantName());
    }

    public static CompanyDetailDTO toDetailDTO(Company c) {
        List<PlantDTO> plants = (c.getPlants() == null) ? List.of()
                : c.getPlants().stream().map(CompanyMapper::toPlantDTO).toList();

        return new CompanyDetailDTO(c.getCompanyId(), c.getCompanyName(), plants);
    }
}
