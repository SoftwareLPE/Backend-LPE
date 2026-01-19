package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.RoleSummaryDTO;
import com.example.backend_sistema_LPE.service.RoleAdminSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleAdminSummaryController {
    private final RoleAdminSummaryService roleAdminSummaryService;

    @GetMapping("/summary")
    public List<RoleSummaryDTO> getRolesSummary() {
        return roleAdminSummaryService.getRolesSummary();
    }
}
