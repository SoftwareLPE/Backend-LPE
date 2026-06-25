package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "wialon_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SessionWialon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wialonId;

    @Column(nullable = false, length = 255)
    private String sid;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(nullable = false)
    private Timestamp expiresAt;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private Timestamp lastUsedAt;

    @PrePersist
    void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (createdAt == null) {
            createdAt = now;
        }
        if (lastUsedAt == null) {
            lastUsedAt = now;
        }
    }
}
