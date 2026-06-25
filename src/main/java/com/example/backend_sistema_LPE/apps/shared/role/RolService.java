package com.example.backend_sistema_LPE.apps.shared.role;

import java.util.List;

public interface RolService {

    List<Role> getAllRoles();

    RoleDTO createRole(CreateRoleRequestDTO dto);
}
