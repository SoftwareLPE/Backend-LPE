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
public class WialonSessionStatusResponse {
    private boolean active;
    private String message;
    private String sid;
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private Timestamp lastUsedAt;
    private boolean expired;
}
