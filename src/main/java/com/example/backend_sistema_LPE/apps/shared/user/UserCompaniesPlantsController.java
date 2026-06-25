package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyDetailDTO;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyVisibilityService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/me")
public class UserCompaniesPlantsController {
    private final CompanyVisibilityService companyVisibilityService;

    public UserCompaniesPlantsController(CompanyVisibilityService companyVisibilityService) {
        this.companyVisibilityService = companyVisibilityService;
    }

    @GetMapping("/companies-plants")
    public List<CompanyDetailDTO> getMyCompaniesAndPlants(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return companyVisibilityService.getMyCompanies(principal);
    }
}
