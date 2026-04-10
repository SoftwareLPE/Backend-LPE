package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.enums.CascadaStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

final class SentStatusPreserver {

    private SentStatusPreserver() {
    }

    static <T> Metadata from(
            Collection<T> items,
            Function<T, CascadaStatus> statusGetter,
            Function<T, LocalDateTime> sentAtGetter,
            Function<T, Long> sentByUserIdGetter
    ) {
        if (items == null || items.isEmpty()) {
            return Metadata.draft();
        }

        boolean preserveSent = items.stream()
                .map(statusGetter)
                .anyMatch(CascadaStatus.SENT::equals);
        if (!preserveSent) {
            return Metadata.draft();
        }

        LocalDateTime sentAt = items.stream()
                .map(sentAtGetter)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        Long sentByUserId = items.stream()
                .map(sentByUserIdGetter)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return new Metadata(CascadaStatus.SENT, sentAt, sentByUserId);
    }

    record Metadata(CascadaStatus status, LocalDateTime sentAt, Long sentByUserId) {
        static Metadata draft() {
            return new Metadata(CascadaStatus.DRAFT, null, null);
        }

        boolean isSent() {
            return status == CascadaStatus.SENT;
        }
    }
}
