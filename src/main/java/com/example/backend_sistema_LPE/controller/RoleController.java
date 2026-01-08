package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.service.RolService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/roles")
public class RoleController {
    private final RolService rolService;

    public RoleController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping
    public List<Role> getAllRoles(){
        return rolService.getAllRoles();
    }
}
