package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//Dto para que el backend regrese el token de acceso al usuario logueado
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String username;
    private List<String> roles;

    // Solo relevante para ROLE_COORDINADOR_PLANTA
    private Long plantId;
    private String plantName;
    private Long companyId;
    private String companyName;

}
