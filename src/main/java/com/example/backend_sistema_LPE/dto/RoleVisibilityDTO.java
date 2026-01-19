package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;

@Getter
@Setter
public class RoleVisibilityDTO {
    @NotNull
    private Long companyId;

    // null o [] => todas las plantas de esa compañía
    private List<Long> plantIds;
}
