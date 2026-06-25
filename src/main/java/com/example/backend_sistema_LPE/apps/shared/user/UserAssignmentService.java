package com.example.backend_sistema_LPE.apps.shared.user;

import java.util.List;


public interface UserAssignmentService {

     void assignCompaniesToUser(Long userId, List<Long> companyIds);

     void assignPlantsToUser(Long userId, List<Long> plantIds);

     void replaceCompaniesForUser(Long userId, List<Long> companyIds);

     void replacePlantsForUser(Long userId, List<Long> plantIds);

}
