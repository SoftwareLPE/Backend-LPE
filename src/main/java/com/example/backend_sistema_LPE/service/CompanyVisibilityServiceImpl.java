package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CompanyDetailDTO;
import com.example.backend_sistema_LPE.dto.PlantDTO;
import com.example.backend_sistema_LPE.mapper.CompanyMapper;
import com.example.backend_sistema_LPE.model.Company;
import com.example.backend_sistema_LPE.repository.CompanyRepository;
import com.example.backend_sistema_LPE.repository.UserCompanyRepository;
import com.example.backend_sistema_LPE.repository.UserPlantRepository;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompanyVisibilityServiceImpl implements CompanyVisibilityService{
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserPlantRepository userPlantRepository;

    public CompanyVisibilityServiceImpl(
            CompanyRepository companyRepository,
            UserCompanyRepository userCompanyRepository,
            UserPlantRepository userPlantRepository
    ) {
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.userPlantRepository = userPlantRepository;
    }


    @Override
    public List<CompanyDetailDTO> getMyCompanies(UserPrincipal userPrincipal) {
        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));

        if (isAdmin){
            return companyRepository.findAllWithPlants().stream()
                    .map(CompanyMapper::toDetailDTO)
                    .toList();
        }

        Set<Long> assignedPlantIds = new LinkedHashSet<>(userPlantRepository.findPlantIdsByUserId(userPrincipal.getUserId()));
        if (assignedPlantIds.isEmpty()) {
            return List.of();
        }

        return userCompanyRepository.findCompanyIdsByUserId(userPrincipal.getUserId()).stream()
                .map(companyRepository::findByIdWithPlants)
                .flatMap(java.util.Optional::stream)
                .map(company -> toFilteredDetailDTO(company, assignedPlantIds))
                .filter(company -> !company.getPlantDTOList().isEmpty())
                .toList();
    }

    private CompanyDetailDTO toFilteredDetailDTO(Company company, Set<Long> assignedPlantIds) {
        List<PlantDTO> plants = company.getPlants() == null
                ? List.of()
                : company.getPlants().stream()
                .filter(plant -> plant.getPlantId() != null && assignedPlantIds.contains(plant.getPlantId()))
                .map(CompanyMapper::toPlantDTO)
                .toList();

        return new CompanyDetailDTO(
                company.getCompanyId(),
                company.getCompanyName(),
                plants
        );
    }
}
