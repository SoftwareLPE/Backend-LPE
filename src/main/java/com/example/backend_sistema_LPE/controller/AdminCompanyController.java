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

    //Lista de todas las compañias
    @GetMapping
    public List<CompanyListDTO> getAllCompanies(){
        return companyAdminService.getAllCompanies();
    }

    //Cuando se abre el modal para editar compañias se ejecuta este endpoint
    @GetMapping("/{companyId:\\d+}")
    public ResponseEntity<CompanyDetailDTO> getCompanyDetail(@PathVariable Long companyId) {
        return ResponseEntity.ok(companyAdminService.getCompanyDetail(companyId));
    }

    //Endpoint que muestra las compañias con el numero de plantas en formato tabla en el frontend
    @GetMapping("/table")
    public ResponseEntity<List<CompanyTableDTO>> getCompaniesTable() {
        return ResponseEntity.ok(companyAdminService.getCompaniesForTable());
    }

    //Endpoint que muestra todas las companias con sus plantas (para admin)
    @GetMapping("/with-plants")
    public ResponseEntity<List<CompanyDetailDTO>> getCompaniesWithPlants() {
        return ResponseEntity.ok(companyAdminService.getAllCompaniesWithPlants());
    }

    //Endpoint el cual crea una compañia con sus respectivas plantas
    @PostMapping
    // @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Company> createCompany(@RequestBody CreateCompanyRequestDTO createCompanyRequestDTO){
        Company company = companyAdminService.createCompany(createCompanyRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    //Endpoint para actualizar el nombre de las compañias
    @PatchMapping("{companyId:\\d+}")
    public ResponseEntity<UpdateCompanyNameDTO> updateCompanyName(
            @PathVariable Long companyId,
            @RequestBody  UpdateCompanyNameDTO updateCompanyNameDTO){
        return ResponseEntity.ok(companyAdminService.updateCompanyName(companyId,updateCompanyNameDTO));
    }

    //Endpoint para agregar plantas a una compañia existente
    @PostMapping("/{companyId:\\d+}/plants")
    //@PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<PlantDTO> addPlantToCompany(
           @PathVariable Long companyId,
           @RequestBody CreateRequestPlantDTO createRequestPlantDTO){

        PlantDTO plantCreated = companyAdminService.addPlantToCompany(companyId,createRequestPlantDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(plantCreated);

    }

    //Endpoint para eliminar companias
    @DeleteMapping("/{companyId:\\d+}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        companyAdminService.deleteCompany(companyId);
        return ResponseEntity.noContent().build();
    }
}
