package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyServiceImpl implements CompanyService{
    private final CompanyRepository companyRepository;

    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public Optional<Company> getCompanyById( Long companyId) {
        return companyRepository.findById(companyId);
    }
}
