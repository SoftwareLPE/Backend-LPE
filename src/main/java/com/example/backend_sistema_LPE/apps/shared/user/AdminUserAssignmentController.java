package com.example.backend_sistema_LPE.apps.shared.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("admin/users")
public class AdminUserAssignmentController {
    private final UserAssignmentService userAssignmentService;


    //Endpoint que asigna compañias a usuarios
    public AdminUserAssignmentController(UserAssignmentService userAssignmentService) {
        this.userAssignmentService = userAssignmentService;
    }
    @PutMapping("/{userId}/companies")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> assignCompanies(@PathVariable Long userId, @RequestBody List<Long> companyIds){
        userAssignmentService.assignCompaniesToUser(userId,companyIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/plants")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> replacePlants(@PathVariable Long userId, @RequestBody List<Long> plantIds) {
        userAssignmentService.replacePlantsForUser(userId, plantIds);
        return ResponseEntity.ok().build();
    }
}
