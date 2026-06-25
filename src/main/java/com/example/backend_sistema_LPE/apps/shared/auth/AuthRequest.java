package com.example.backend_sistema_LPE.apps.shared.auth;

import lombok.Data;

//Dto para que el frontend envie las credenciales
@Data
public class AuthRequest {
    private String username;
    private String password;
}
