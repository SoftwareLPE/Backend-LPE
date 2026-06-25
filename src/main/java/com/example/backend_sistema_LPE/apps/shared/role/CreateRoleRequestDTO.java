package com.example.backend_sistema_LPE.apps.shared.role;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Getter
@Setter
public class CreateRoleRequestDTO {
    private String roleKey;

    @NotBlank
    private String roleName;

    private String description;

    // RecomendaciÃ³n: enviar IDs de permisos
    @NotEmpty
    private List<Long> permissionIds;
}
