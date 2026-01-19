package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserTableDTO {
    private Long userId;
    private String fullname;
    private String userName;
    private String email;
    private Boolean active;
    private Long roleId;
    private String roleName;


}
