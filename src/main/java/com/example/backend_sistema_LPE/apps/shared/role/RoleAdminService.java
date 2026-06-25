package com.example.backend_sistema_LPE.apps.shared.role;

public interface RoleAdminService {
    RoleDTO createRole(CreateRoleRequestDTO dto);

    RoleDTO updateRole(Long roleId, UpdateRoleRequestDTO dto);

    RoleDetailDTO getRoleDetail(Long roleId);

    public void deleteRole(Long roleId);
}
