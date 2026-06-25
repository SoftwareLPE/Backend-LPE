package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;

import java.util.List;

public interface CompanyVisibilityService {
    List<CompanyDetailDTO> getMyCompanies(UserPrincipal userPrincipal);
}
