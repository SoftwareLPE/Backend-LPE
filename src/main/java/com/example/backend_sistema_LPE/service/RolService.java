package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateRoleRequestDTO;
import com.example.backend_sistema_LPE.dto.RoleDTO;
import com.example.backend_sistema_LPE.model.Role;

import java.util.List;
import java.util.Optional;

public interface RolService {

    List<Role> getAllRoles();

    RoleDTO createRole(CreateRoleRequestDTO dto);
}
