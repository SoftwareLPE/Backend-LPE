package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.plant.PlantDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyReadController {
    private final CompanyRepository companyRepository;

    public CompanyReadController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/{companyId}/plants")
    public ResponseEntity<List<PlantDTO>> getCompanyPlants(@PathVariable Long companyId) {
        return companyRepository.findByIdWithPlants(companyId)
                .map(company -> {
                    List<PlantDTO> plants = company.getPlants() == null
                            ? List.of()
                            : company.getPlants().stream().map(CompanyMapper::toPlantDTO).toList();
                    return ResponseEntity.ok(plants);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
