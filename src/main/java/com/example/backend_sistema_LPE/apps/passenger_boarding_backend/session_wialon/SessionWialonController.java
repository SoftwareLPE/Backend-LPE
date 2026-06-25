package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.dto.WialonSessionRefreshResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon.dto.WialonSessionStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.Optional;

@RestController
@RequestMapping("/api/wialon/session")
public class SessionWialonController {

    private final SessionWialonService sessionWialonService;

    public SessionWialonController(SessionWialonService sessionWialonService) {
        this.sessionWialonService = sessionWialonService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<WialonSessionRefreshResponse> refreshSession() {
        String sid = sessionWialonService.getOrCreateValidSid();
        Optional<SessionWialon> activeSession = sessionWialonService.findActiveSession();

        WialonSessionRefreshResponse response = new WialonSessionRefreshResponse(
                "Wialon session refreshed",
                sid,
                activeSession.map(SessionWialon::getExpiresAt).orElse(null)
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<WialonSessionStatusResponse> getSessionStatus() {
        Optional<SessionWialon> activeSession = sessionWialonService.findActiveSession();

        if (activeSession.isEmpty()) {
            WialonSessionStatusResponse response = new WialonSessionStatusResponse(
                    false,
                    "No active Wialon session",
                    null,
                    null,
                    null,
                    null,
                    true
            );
            return ResponseEntity.ok(response);
        }

        SessionWialon session = activeSession.get();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        boolean expired = session.getExpiresAt() == null || session.getExpiresAt().before(now);
        WialonSessionStatusResponse response = new WialonSessionStatusResponse(
                session.isActive(),
                "Active Wialon session found",
                session.getSid(),
                session.getCreatedAt(),
                session.getExpiresAt(),
                session.getLastUsedAt(),
                expired
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
