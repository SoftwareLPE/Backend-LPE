package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Company;

import java.util.List;
import java.util.Optional;

public interface CompanyService {

    List<Company> getAllCompanies();

    Optional<Company> getCompanyById(Long companyId);



};
