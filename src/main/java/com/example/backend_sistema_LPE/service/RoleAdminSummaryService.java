package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RoleSummaryDTO;

import java.util.List;

public interface RoleAdminSummaryService {
    List<RoleSummaryDTO> getRolesSummary();
}
