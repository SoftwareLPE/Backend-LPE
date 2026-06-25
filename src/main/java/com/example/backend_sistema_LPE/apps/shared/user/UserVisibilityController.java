package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyDetailDTO;
import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyVisibilityService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/me")
public class UserVisibilityController {
    private final CompanyVisibilityService companyVisibilityService;

    public UserVisibilityController(CompanyVisibilityService companyVisibilityService) {
        this.companyVisibilityService = companyVisibilityService;
    }

    //Endpoint que muestra las compañias correspondientes segun fueran asignadas
    @GetMapping("/companies")
    public List<CompanyDetailDTO> myCompanies(Authentication authentication){;

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        System.out.println("JWT userId=" + principal.getUserId() + " auth=" + principal.getAuthorities());
        return companyVisibilityService.getMyCompanies(principal);
    }
}
