package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.plant.CreateRequestPlantDTO;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantDTO;

import java.util.List;

public interface CompanyAdminService {
    List<CompanyListDTO> getAllCompanies();

    CompanyDetailDTO getCompanyDetail(Long companyId);

    List<CompanyTableDTO> getCompaniesForTable();

    PlantDTO addPlantToCompany(Long companyId, CreateRequestPlantDTO dto);

    List<CompanyDetailDTO> getAllCompaniesWithPlants();

    Company createCompany(CreateCompanyRequestDTO createCompanyRequestDTO);

    UpdateCompanyNameDTO updateCompanyName(Long companyId, UpdateCompanyNameDTO updateCompanyNameDTO);

    void deleteCompany(Long companyId);

}
