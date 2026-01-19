package com.example.backend_sistema_LPE.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;


public interface UserAssignmentService {

     void assignCompaniesToUser(Long userId, List<Long> companyIds);

     void assignPlantsToUser(Long userId, List<Long> plantIds);

     void replaceCompaniesForUser(Long userId, List<Long> companyIds);

     void replacePlantsForUser(Long userId, List<Long> plantIds);

}
