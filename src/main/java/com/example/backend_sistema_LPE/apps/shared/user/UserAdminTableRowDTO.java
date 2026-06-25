package com.example.backend_sistema_LPE.apps.shared.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserAdminTableRowDTO {
    private Long userId;
    private String fullname;
    private String userName;
    private String email;
    private Boolean active;
    private Long roleId;
    private String roleName;
    private List<Long> companyIds;
    private List<String> companyNames;
    private List<Long> plantIds;
    private List<String> plantNames;
}
