package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface InboxMessageService {
    List<CascadaSummaryDTO> getInboxMessages(String status, Long plantId, LocalDate weekDate, Long recipientUserId);

    void markMessageAsOpened(Long userId, String messageId, String currentVersion);
}
