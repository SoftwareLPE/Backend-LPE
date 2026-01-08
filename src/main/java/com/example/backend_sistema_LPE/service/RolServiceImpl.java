package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateRoleRequestDTO;
import com.example.backend_sistema_LPE.dto.RoleDTO;
import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServiceImpl implements RolService{
    private final RoleRepository roleRepository;

    public RolServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public RoleDTO createRole(CreateRoleRequestDTO dto) {
        return null;
    }
}
