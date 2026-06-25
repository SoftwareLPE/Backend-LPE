package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WialonSessionRefreshResponse {
    private String message;
    private String sid;
    private Timestamp expiresAt;
}
