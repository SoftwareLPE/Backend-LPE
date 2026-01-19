package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.repository.CompanyRepository;
import com.example.backend_sistema_LPE.repository.UserCompanyRepository;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyVisibilityServiceImpl implements CompanyVisibilityService{
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;

    public CompanyVisibilityServiceImpl(CompanyRepository companyRepository, UserCompanyRepository userCompanyRepository) {
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
    }


    @Override
    public List<Company> getMyCompanies(UserPrincipal userPrincipal) {
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isAdmin){
            return companyRepository.findAll();
        }
        return userCompanyRepository.findCompaniesByUserId(userPrincipal.getUserId());
    }
}
