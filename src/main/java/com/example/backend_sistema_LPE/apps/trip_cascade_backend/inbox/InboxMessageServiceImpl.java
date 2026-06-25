package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard.CascadaStandardService;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur.FlexsurWeekService;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom.FormatWeekService;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal.RegalWeekService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class InboxMessageServiceImpl implements InboxMessageService {
    private final CascadaStandardService cascadaStandardService;
    private final FormatWeekService formatWeekService;
    private final FlexsurWeekService flexsurWeekService;
    private final RegalWeekService regalWeekService;
    private final InboxMessageUserStateService inboxMessageUserStateService;

    public InboxMessageServiceImpl(
            CascadaStandardService cascadaStandardService,
            FormatWeekService formatWeekService,
            FlexsurWeekService flexsurWeekService,
            RegalWeekService regalWeekService,
            InboxMessageUserStateService inboxMessageUserStateService
    ) {
        this.cascadaStandardService = cascadaStandardService;
        this.formatWeekService = formatWeekService;
        this.flexsurWeekService = flexsurWeekService;
        this.regalWeekService = regalWeekService;
        this.inboxMessageUserStateService = inboxMessageUserStateService;
    }

    @Override
    public List<CascadaSummaryDTO> getInboxMessages(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long recipientUserId
    ) {
        List<CascadaSummaryDTO> messages = new ArrayList<>();
        messages.addAll(cascadaStandardService.getCascadaStandardSummaries(status, plantId, weekDate, recipientUserId));
        messages.addAll(formatWeekService.getFormatWeekSummaries(status, plantId, weekDate, recipientUserId));
        messages.addAll(flexsurWeekService.getFlexsurSummaries(status, plantId, weekDate, recipientUserId));
        messages.addAll(regalWeekService.getRegalSummaries(status, plantId, weekDate, recipientUserId));
        inboxMessageUserStateService.applyVisualStatus(messages, recipientUserId);

        messages.sort(Comparator
                .comparing(this::resolveOrderingVersion, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(CascadaSummaryDTO::getSentAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());
        return messages;
    }

    @Override
    public void markMessageAsOpened(Long userId, String messageId, String currentVersion) {
        inboxMessageUserStateService.markAsOpened(userId, messageId, currentVersion);
    }

    private LocalDateTime resolveOrderingVersion(CascadaSummaryDTO summary) {
        if (summary.getCurrentVersion() != null) {
            return summary.getCurrentVersion();
        }
        if (summary.getUpdatedAt() != null) {
            return summary.getUpdatedAt();
        }
        return summary.getSentAt();
    }
}
