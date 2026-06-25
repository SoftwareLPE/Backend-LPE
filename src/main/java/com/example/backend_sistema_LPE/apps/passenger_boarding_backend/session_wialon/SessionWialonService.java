package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import java.sql.Timestamp;
import java.util.Optional;

public interface SessionWialonService {

    Optional<SessionWialon> findActiveSession();

    Optional<SessionWialon> findLatestSession();

    Optional<String> getValidSid();

    SessionWialon registerNewSession(String sid, String token, Timestamp expiresAt);

    int deactivateAllActiveSessions();

    boolean isSessionExpired(SessionWialon session);

    String getOrCreateValidSid();

    String forceRefreshSid();
}
