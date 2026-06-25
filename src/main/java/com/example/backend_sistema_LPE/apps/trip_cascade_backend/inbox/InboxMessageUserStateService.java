package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

import java.util.List;

public interface InboxMessageUserStateService {
    void applyVisualStatus(List<CascadaSummaryDTO> summaries, Long userId);

    void markAsOpened(Long userId, String messageId, String requestedCurrentVersion);
}
