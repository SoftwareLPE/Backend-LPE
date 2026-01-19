package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PermissionDTO {
    private Long permissionId;
    private String code;
    private String description;
}
