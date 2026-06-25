package com.example.backend_sistema_LPE.apps.shared.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCompanyAssignmentRowDTO {
    private Long userId;
    private Long companyId;
    private String companyName;
}
