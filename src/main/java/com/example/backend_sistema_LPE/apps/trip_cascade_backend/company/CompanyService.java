package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import java.util.List;
import java.util.Optional;

public interface CompanyService {

    List<Company> getAllCompanies();

    Optional<Company> getCompanyById(Long companyId);



};
