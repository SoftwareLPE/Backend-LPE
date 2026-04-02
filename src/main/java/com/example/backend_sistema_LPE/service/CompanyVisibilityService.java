package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CompanyDetailDTO;
import com.example.backend_sistema_LPE.security.UserPrincipal;

import java.util.List;

public interface CompanyVisibilityService {
    List<CompanyDetailDTO> getMyCompanies(UserPrincipal userPrincipal);
}
