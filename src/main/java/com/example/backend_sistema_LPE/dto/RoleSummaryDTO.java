package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleSummaryDTO {
    private Long roleId;
    private String roleName;
    private String roleDescription;

    private long permissionsCount;

}
