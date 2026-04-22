package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.model.InboxMessageUserState;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.InboxMessageUserStateRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class InboxMessageUserStateServiceImpl implements InboxMessageUserStateService {
    private static final String VISUAL_STATUS_NEW = "new";
    private static final String VISUAL_STATUS_UPDATED = "updated";
    private static final String VISUAL_STATUS_READ = "read";

    private final InboxMessageUserStateRepository inboxMessageUserStateRepository;
    private final InboxMessageVersionResolver inboxMessageVersionResolver;
    private final UserRepository userRepository;

    public InboxMessageUserStateServiceImpl(
            InboxMessageUserStateRepository inboxMessageUserStateRepository,
            InboxMessageVersionResolver inboxMessageVersionResolver,
            UserRepository userRepository
    ) {
        this.inboxMessageUserStateRepository = inboxMessageUserStateRepository;
        this.inboxMessageVersionResolver = inboxMessageVersionResolver;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void applyVisualStatus(List<CascadaSummaryDTO> summaries, Long userId) {
        if (userId == null || summaries == null || summaries.isEmpty()) {
            return;
        }

        List<String> messageIds = summaries.stream()
                .map(CascadaSummaryDTO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (messageIds.isEmpty()) {
            return;
        }

        Map<String, InboxMessageUserState> stateByMessageId = inboxMessageUserStateRepository
                .findByUserUserIdAndMessageIdIn(userId, messageIds)
                .stream()
                .collect(LinkedHashMap::new, (map, state) -> map.put(state.getMessageId(), state), Map::putAll);

        User user = null;
        List<InboxMessageUserState> statesToSave = new ArrayList<>();
        for (CascadaSummaryDTO summary : summaries) {
            LocalDateTime currentVersion = resolveCurrentVersion(userId, summary);
            summary.setCurrentVersion(currentVersion);

            String messageId = summary.getId();
            if (messageId == null || messageId.isBlank() || currentVersion == null) {
                summary.setVisualStatus(VISUAL_STATUS_NEW);
                continue;
            }

            InboxMessageUserState state = stateByMessageId.get(messageId);
            boolean changed = false;
            if (state == null) {
                if (user == null) {
                    user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                }
                state = new InboxMessageUserState();
                state.setUser(user);
                state.setMessageId(messageId);
                state.setFirstSeenVersion(currentVersion);
                state.setLastSeenVersion(currentVersion);
                stateByMessageId.put(messageId, state);
                changed = true;
            } else {
                if (state.getFirstSeenVersion() == null || currentVersion.isBefore(state.getFirstSeenVersion())) {
                    state.setFirstSeenVersion(currentVersion);
                    changed = true;
                }
                if (state.getLastSeenVersion() == null || currentVersion.isAfter(state.getLastSeenVersion())) {
                    state.setLastSeenVersion(currentVersion);
                    changed = true;
                }
            }

            summary.setVisualStatus(resolveVisualStatus(state, currentVersion));
            if (changed) {
                statesToSave.add(state);
            }
        }

        if (!statesToSave.isEmpty()) {
            inboxMessageUserStateRepository.saveAll(statesToSave);
        }
    }

    @Override
    @Transactional
    public void markAsOpened(Long userId, String messageId, String requestedCurrentVersion) {
        if (userId == null) {
            throw new RuntimeException("userId is required");
        }
        if (messageId == null || messageId.isBlank()) {
            throw new RuntimeException("messageId is required");
        }

        LocalDateTime authoritativeCurrentVersion = inboxMessageVersionResolver
                .resolveCurrentVersionForUser(userId, messageId)
                .orElseThrow(() -> new RuntimeException("Inbox message not found"));
        authoritativeCurrentVersion = normalizeVersion(authoritativeCurrentVersion);
        LocalDateTime requestedVersion = parseVersion(requestedCurrentVersion);
        LocalDateTime safeVersion = resolveSafeOpenedVersion(requestedVersion, authoritativeCurrentVersion);

        InboxMessageUserState state = inboxMessageUserStateRepository
                .findByUserUserIdAndMessageId(userId, messageId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    InboxMessageUserState newState = new InboxMessageUserState();
                    newState.setUser(user);
                    newState.setMessageId(messageId);
                    return newState;
                });

        LocalDateTime existingFirstSeenVersion = normalizeVersion(state.getFirstSeenVersion());
        LocalDateTime existingLastSeenVersion = normalizeVersion(state.getLastSeenVersion());
        LocalDateTime existingOpenedVersion = normalizeVersion(state.getOpenedVersion());

        if (existingFirstSeenVersion == null || safeVersion.isBefore(existingFirstSeenVersion)) {
            state.setFirstSeenVersion(safeVersion);
        }
        if (existingLastSeenVersion == null || safeVersion.isAfter(existingLastSeenVersion)) {
            state.setLastSeenVersion(safeVersion);
        }
        if (existingOpenedVersion == null || safeVersion.isAfter(existingOpenedVersion)) {
            state.setOpenedVersion(safeVersion);
        }

        inboxMessageUserStateRepository.save(state);
    }

    private LocalDateTime resolveCurrentVersion(Long userId, CascadaSummaryDTO summary) {
        if (summary == null) {
            return null;
        }
        String messageId = summary.getId();
        if (messageId != null && !messageId.isBlank()) {
            LocalDateTime authoritativeVersion = inboxMessageVersionResolver
                    .resolveCurrentVersionForUser(userId, messageId)
                    .orElse(null);
            if (authoritativeVersion != null) {
                return normalizeVersion(authoritativeVersion);
            }
        }
        LocalDateTime fallbackVersion = summary.getUpdatedAt() != null ? summary.getUpdatedAt() : summary.getSentAt();
        return normalizeVersion(fallbackVersion);
    }

    private String resolveVisualStatus(InboxMessageUserState state, LocalDateTime currentVersion) {
        if (state == null || currentVersion == null) {
            return VISUAL_STATUS_NEW;
        }
        LocalDateTime openedVersion = normalizeVersion(state.getOpenedVersion());
        if (openedVersion != null && !openedVersion.isBefore(currentVersion)) {
            return VISUAL_STATUS_READ;
        }
        LocalDateTime firstSeenVersion = normalizeVersion(state.getFirstSeenVersion());
        if (firstSeenVersion != null && currentVersion.isAfter(firstSeenVersion)) {
            return VISUAL_STATUS_UPDATED;
        }
        return VISUAL_STATUS_NEW;
    }

    private LocalDateTime resolveSafeOpenedVersion(LocalDateTime requestedVersion, LocalDateTime authoritativeVersion) {
        if (requestedVersion == null) {
            return authoritativeVersion;
        }
        if (requestedVersion.isAfter(authoritativeVersion)) {
            return authoritativeVersion;
        }
        return requestedVersion;
    }

    private LocalDateTime parseVersion(String rawVersion) {
        if (rawVersion == null || rawVersion.isBlank()) {
            return null;
        }

        try {
            return normalizeVersion(LocalDateTime.parse(rawVersion.trim()));
        } catch (DateTimeParseException ignored) {
        }

        try {
            return normalizeVersion(OffsetDateTime.parse(rawVersion.trim()).toLocalDateTime());
        } catch (DateTimeParseException ex) {
            throw new RuntimeException("Invalid currentVersion format");
        }
    }

    private LocalDateTime normalizeVersion(LocalDateTime version) {
        if (version == null) {
            return null;
        }
        return version.truncatedTo(ChronoUnit.SECONDS);
    }
}
