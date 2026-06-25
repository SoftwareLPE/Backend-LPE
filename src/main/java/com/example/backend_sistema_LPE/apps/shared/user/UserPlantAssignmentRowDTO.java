package com.example.backend_sistema_LPE.apps.shared.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserPlantAssignmentRowDTO {
    private Long userId;
    private Long plantId;
    private String plantName;
}
