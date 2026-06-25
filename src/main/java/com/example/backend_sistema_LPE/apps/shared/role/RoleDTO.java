package com.example.backend_sistema_LPE.apps.shared.role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleDTO {
    private Long roleId;
    private String roleKey;
    private String roleName;
    private String description;
}
