package com.example.backend_sistema_LPE.apps.shared.role;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Getter
@Setter
public class UpdateRoleRequestDTO {
    private String roleKey;

    @NotBlank
    private String roleName;

    private String description;

    @NotEmpty
    private List<Long> permissionIds;
}

