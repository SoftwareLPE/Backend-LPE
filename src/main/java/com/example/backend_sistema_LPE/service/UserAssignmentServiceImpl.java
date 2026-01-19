package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.model.*;
import com.example.backend_sistema_LPE.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAssignmentServiceImpl implements UserAssignmentService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserCompanyRepository userCompanyRepository;

    private final PlantRepository plantRepository;               // NUEVO
    private final UserPlantRepository userPlantRepository;

    public UserAssignmentServiceImpl(UserRepository userRepository, CompanyRepository companyRepository, UserCompanyRepository userCompanyRepository, PlantRepository plantRepository, UserPlantRepository userPlantRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.plantRepository = plantRepository;
        this.userPlantRepository = userPlantRepository;
    }


    @Override
    @Transactional
    public void assignCompaniesToUser(Long userId, List<Long> companyIds) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        for (Long companyId : companyIds) {
            if (userCompanyRepository.existsByUserUserIdAndCompanyCompanyId(userId, companyId)) {
                continue;
            }
            Company company = companyRepository.findById(companyId).orElseThrow(() -> new RuntimeException("company not found"));

            UserCompany userCompany = new UserCompany();
            userCompany.setUser(user);
            userCompany.setCompany(company);
            userCompany.setAssignmentDate(LocalDateTime.now());

            userCompanyRepository.save(userCompany);
        }
    }

    @Override
    @Transactional
    public void assignPlantsToUser(Long userId, List<Long> plantIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        for (Long plantId : plantIds) {

            if (userPlantRepository.existsByUserUserIdAndPlantPlantId(userId, plantId)) {
                continue;
            }

            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));

            UserPlant userPlant = new UserPlant();
            userPlant.setUser(user);
            userPlant.setPlant(plant);
            userPlant.setAssignmentDate(LocalDateTime.now());

            userPlantRepository.save(userPlant);
        }

    }

    @Override
    @Transactional

    public void replaceCompaniesForUser(Long userId, List<Long> companyIds) {
        userCompanyRepository.deleteByUserUserId(userId);

        if (companyIds == null || companyIds.isEmpty()) {
            return;
        }

        assignCompaniesToUser(userId, companyIds);
    }

    @Override
    @Transactional
    public void replacePlantsForUser(Long userId, List<Long> plantIds) {
        userPlantRepository.deleteByUserUserId(userId);

        if (plantIds == null || plantIds.isEmpty()) {
            return;
        }

        assignPlantsToUser(userId, plantIds);
    }
}

