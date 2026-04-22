package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;

import java.util.List;

public interface InboxMessageUserStateService {
    void applyVisualStatus(List<CascadaSummaryDTO> summaries, Long userId);

    void markAsOpened(Long userId, String messageId, String requestedCurrentVersion);
}
