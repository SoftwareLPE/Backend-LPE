package com.example.backend_sistema_LPE.apps.shared.role;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class RoleDetailDTO {
    private Long roleId;
    private String roleKey;
    private String roleName;
    private String roleDescription;

    private List<Long> permissionIds;
}
