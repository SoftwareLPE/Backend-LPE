package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InboxMessageVersionResolver {
    Optional<LocalDateTime> resolveCurrentVersionForUser(Long userId, String messageId);
}
