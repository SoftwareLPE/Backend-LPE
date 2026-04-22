package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;

import java.time.LocalDate;
import java.util.List;

public interface InboxMessageService {
    List<CascadaSummaryDTO> getInboxMessages(String status, Long plantId, LocalDate weekDate, Long recipientUserId);

    void markMessageAsOpened(Long userId, String messageId, String currentVersion);
}
