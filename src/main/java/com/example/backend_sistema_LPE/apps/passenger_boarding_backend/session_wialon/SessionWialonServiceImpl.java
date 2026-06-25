package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@Transactional
public class SessionWialonServiceImpl implements SessionWialonService {

    private final SessionWialonRepository sessionWialonRepository;
    private final WialonAuthClient wialonAuthClient;
    private final String wialonToken;
    private final long sessionTtlMinutes;


    public SessionWialonServiceImpl(
            SessionWialonRepository sessionWialonRepository,
            WialonAuthClient wialonAuthClient,
            @Value("${wialon.api.token}") String wialonToken,
            @Value("${wialon.api.session-ttl-minutes:30}") long sessionTtlMinutes
    ) {
        this.sessionWialonRepository = sessionWialonRepository;
        this.wialonAuthClient = wialonAuthClient;
        this.wialonToken = wialonToken;
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    @Override
    public Optional<SessionWialon> findActiveSession() {
        return sessionWialonRepository.findTopByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Override
    public Optional<SessionWialon> findLatestSession() {
        return sessionWialonRepository.findTopByOrderByCreatedAtDesc();
    }

    @Override
    public Optional<String> getValidSid() {
        Optional<SessionWialon> activeSession = findActiveSession();
        if (activeSession.isEmpty()) {
            return Optional.empty();
        }

        SessionWialon session = activeSession.get();
        if (isSessionExpired(session)) {
            session.setActive(false);
            sessionWialonRepository.save(session);
            return Optional.empty();
        }

        session.setLastUsedAt(Timestamp.from(Instant.now()));
        sessionWialonRepository.save(session);
        return Optional.of(session.getSid());
    }

    @Override
    public SessionWialon registerNewSession(String sid, String token, Timestamp expiresAt) {
        deactivateAllActiveSessions();

        Timestamp now = Timestamp.from(Instant.now());
        SessionWialon session = new SessionWialon();
        session.setSid(sid);
        session.setToken(token);
        session.setCreatedAt(now);
        session.setExpiresAt(expiresAt);
        session.setLastUsedAt(now);
        session.setActive(true);

        return sessionWialonRepository.save(session);
    }

    @Override
    public int deactivateAllActiveSessions() {
        return sessionWialonRepository.deactivateAllActiveSessions();
    }

    @Override
    public boolean isSessionExpired(SessionWialon session) {
        if (session == null || session.getExpiresAt() == null) {
            return true;
        }

        return session.getExpiresAt().before(Timestamp.from(Instant.now()));
    }

    @Override
    public String getOrCreateValidSid() {
        Optional<String> validSid = getValidSid();
        if (validSid.isPresent()) {
            return validSid.get();
        }

        return forceRefreshSid();
    }

    @Override
    public String forceRefreshSid() {
        String sid = wialonAuthClient.tokenLogin();
        Instant expiresAtInstant = Instant.now().plus(sessionTtlMinutes, ChronoUnit.MINUTES);
        Timestamp expiresAt = Timestamp.from(expiresAtInstant);

        SessionWialon savedSession = registerNewSession(sid, wialonToken, expiresAt);
        return savedSession.getSid();
    }
}
