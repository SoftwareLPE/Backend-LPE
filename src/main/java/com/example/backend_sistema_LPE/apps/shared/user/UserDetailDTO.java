package com.example.backend_sistema_LPE.apps.shared.user;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDetailDTO {

    private String name;
    private String lastName;
    private String userName;
    private String email;
    private Boolean active;

    private Long roleId;

    private List<Long> companyIds;
    private List<Long> plantIds;
}
