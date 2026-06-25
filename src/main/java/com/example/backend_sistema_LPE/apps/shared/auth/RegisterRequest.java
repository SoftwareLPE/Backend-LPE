package com.example.backend_sistema_LPE.apps.shared.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private Boolean active;
    private Long roleId;
}
