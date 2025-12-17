package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.service.CompanyService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;

@RestController
@RequestMapping("/companies")
@CrossOrigin(origins ="http://localhost:8081/")
public class CompanyController {
    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public List<Company> getAllCompanies(){
        return companyService.getAllCompanies();
    }

    @GetMapping("{companyId}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long companyId) {
        return companyService.getCompanyById(companyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}
