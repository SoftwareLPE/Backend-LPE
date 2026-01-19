package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.*;
import com.example.backend_sistema_LPE.model.Company;

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
