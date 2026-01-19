package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.security.UserPrincipal;

import java.util.List;

public interface CompanyVisibilityService {
    List<Company> getMyCompanies(UserPrincipal userPrincipal);
}
