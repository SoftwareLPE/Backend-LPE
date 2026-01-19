package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateRoleRequestDTO;
import com.example.backend_sistema_LPE.dto.RoleDTO;
import com.example.backend_sistema_LPE.dto.RoleDetailDTO;
import com.example.backend_sistema_LPE.dto.UpdateRoleRequestDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleAdminService {
    RoleDTO createRole(CreateRoleRequestDTO dto);

    RoleDTO updateRole(Long roleId, UpdateRoleRequestDTO dto);

    RoleDetailDTO getRoleDetail(Long roleId);

    public void deleteRole(Long roleId);
}
