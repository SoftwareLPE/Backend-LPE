package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.*;
import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.service.CompanyAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/companies")
public class AdminCompanyController {
    private final CompanyAdminService companyAdminService;

    public AdminCompanyController(CompanyAdminService companyAdminService) {
        this.companyAdminService = companyAdminService;
    }


    @GetMapping
    public List<CompanyListDTO> getAllCompanies(){
        return companyAdminService.getAllCompanies();
    }

    @GetMapping("/{companyId:\\d+}")
    public ResponseEntity<CompanyDetailDTO> getCompanyDetail(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyAdminService.getCompanyDetail(companyId));
    }

    @GetMapping("/table")
    public ResponseEntity<List<CompanyTableDTO>> getCompaniesTable() {
        return ResponseEntity.ok(companyAdminService.getCompaniesForTable());
    }

    @GetMapping("/with-plants")
    public ResponseEntity<List<CompanyDetailDTO>> getCompaniesWithPlants() {
        return ResponseEntity.ok(companyAdminService.getAllCompaniesWithPlants());
    }

    @PostMapping
    // @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Company> createCompany(@RequestBody CreateCompanyRequestDTO createCompanyRequestDTO){
        Company company = companyAdminService.createCompany(createCompanyRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @PatchMapping("{companyId:\\d+}")
    public ResponseEntity<UpdateCompanyNameDTO> updateCompanyName(
            @PathVariable Long companyId,
            @RequestBody  UpdateCompanyNameDTO updateCompanyNameDTO){
        return ResponseEntity.ok(companyAdminService.updateCompanyName(companyId,updateCompanyNameDTO));
    }

    @PostMapping("/{companyId:\\d+}/plants")
    public ResponseEntity<PlantDTO> addPlantToCompany(
           @PathVariable Long companyId,
           @RequestBody CreateRequestPlantDTO createRequestPlantDTO){

        PlantDTO plantCreated = companyAdminService.addPlantToCompany(companyId,createRequestPlantDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(plantCreated);

    }

    @DeleteMapping("/{companyId:\\d+}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        companyAdminService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
