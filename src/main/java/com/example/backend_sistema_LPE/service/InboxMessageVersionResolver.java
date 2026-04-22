package com.example.backend_sistema_LPE.service;

import java.time.LocalDateTime;
import java.util.Optional;

public interface InboxMessageVersionResolver {
    Optional<LocalDateTime> resolveCurrentVersionForUser(Long userId, String messageId);
}
