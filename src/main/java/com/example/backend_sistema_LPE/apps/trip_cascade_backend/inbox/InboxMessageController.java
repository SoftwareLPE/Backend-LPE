package com.example.backend_sistema_LPE.apps.trip_cascade_backend.inbox;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/inbox/messages")
public class InboxMessageController {
    private final InboxMessageService inboxMessageService;

    public InboxMessageController(InboxMessageService inboxMessageService) {
        this.inboxMessageService = inboxMessageService;
    }

    @GetMapping
    public ResponseEntity<List<CascadaSummaryDTO>> getInboxMessages(
            @RequestParam(defaultValue = "SENT") String status,
            @RequestParam(required = false) Long plantId,
            @RequestParam(name = "weekDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekDate,
            @RequestParam(required = false) Long recipientUserId,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(403).build();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMINISTRADOR".equals(a.getAuthority()));
        Long effectiveRecipientId = isAdmin && recipientUserId != null
                ? recipientUserId
                : principal.getUserId();
        return ResponseEntity.ok(
                inboxMessageService.getInboxMessages(status, plantId, weekDate, effectiveRecipientId)
        );
    }

    @PostMapping("/{messageId}/open")
    public ResponseEntity<Void> openMessage(
            @PathVariable String messageId,
            @RequestBody(required = false) InboxMessageOpenRequestDTO request,
            Authentication authentication
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return ResponseEntity.status(403).build();
        }

        inboxMessageService.markMessageAsOpened(
                principal.getUserId(),
                messageId,
                request == null ? null : request.getCurrentVersion()
        );
        return ResponseEntity.noContent().build();
    }
}
