package com.example.backend_sistema_LPE.apps.shared.role;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter
@Setter
public class RoleCompanyVisibilityDTO {
    @NotNull
    private Long companyId;

    // Si viene null o vacío => “todas las plantas de la compañía”
    private List<Long> plantIds;
}
