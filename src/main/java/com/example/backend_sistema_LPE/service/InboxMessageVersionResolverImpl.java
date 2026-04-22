package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.enums.CascadaType;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.CascadaStandardWeek;
import com.example.backend_sistema_LPE.model.FlexsurWeek;
import com.example.backend_sistema_LPE.model.FormatWeek;
import com.example.backend_sistema_LPE.model.RegalWeek;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.CascadaStandardWeekRepository;
import com.example.backend_sistema_LPE.repository.FlexsurWeekRepository;
import com.example.backend_sistema_LPE.repository.FormatWeekRepository;
import com.example.backend_sistema_LPE.repository.RegalWeekRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InboxMessageVersionResolverImpl implements InboxMessageVersionResolver {
    private static final Pattern MESSAGE_ID_PATTERN =
            Pattern.compile("^(standard|custom|flexsur|regal)-(\\d+)-(\\d{4}-\\d{2}-\\d{2})$");
    private static final String FLEXSUR_ALL_SHIFTS_RECIPIENT_KEY = "FLEXSUR";
    private static final String REGAL_RECIPIENT_SHIFT_KEY = "REGAL";

    private final CascadaStandardWeekRepository cascadaStandardWeekRepository;
    private final FormatWeekRepository formatWeekRepository;
    private final FlexsurWeekRepository flexsurWeekRepository;
    private final RegalWeekRepository regalWeekRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;

    public InboxMessageVersionResolverImpl(
            CascadaStandardWeekRepository cascadaStandardWeekRepository,
            FormatWeekRepository formatWeekRepository,
            FlexsurWeekRepository flexsurWeekRepository,
            RegalWeekRepository regalWeekRepository,
            CascadaRecipientRepository cascadaRecipientRepository
    ) {
        this.cascadaStandardWeekRepository = cascadaStandardWeekRepository;
        this.formatWeekRepository = formatWeekRepository;
        this.flexsurWeekRepository = flexsurWeekRepository;
        this.regalWeekRepository = regalWeekRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
    }

    @Override
    public Optional<LocalDateTime> resolveCurrentVersionForUser(Long userId, String messageId) {
        if (userId == null || messageId == null || messageId.isBlank()) {
            return Optional.empty();
        }
        ParsedMessageId parsedMessageId = parseMessageId(messageId).orElse(null);
        if (parsedMessageId == null) {
            return Optional.empty();
        }

        return switch (parsedMessageId.type()) {
            case "standard" -> resolveStandardCurrentVersion(userId, parsedMessageId);
            case "custom" -> resolveCustomCurrentVersion(userId, parsedMessageId);
            case "flexsur" -> resolveFlexsurCurrentVersion(userId, parsedMessageId);
            case "regal" -> resolveRegalCurrentVersion(userId, parsedMessageId);
            default -> Optional.empty();
        };
    }

    private Optional<LocalDateTime> resolveStandardCurrentVersion(Long userId, ParsedMessageId parsedMessageId) {
        List<CascadaStandardWeek> weeks = cascadaStandardWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndStatus(
                        parsedMessageId.plantId(),
                        parsedMessageId.weekStartDate(),
                        CascadaStatus.SENT
                );
        if (weeks.isEmpty()) {
            return Optional.empty();
        }

        Set<String> allowedKeys = allowedRecipientKeys(userId, CascadaType.STANDARD);
        List<CascadaStandardWeek> visibleWeeks = weeks.stream()
                .filter(week -> allowedKeys.contains(
                        week.getPlant().getPlantId() + "|" + week.getWeekStartDate() + "|" + week.getShiftId()
                ))
                .toList();

        return resolveCurrentVersion(visibleWeeks.stream()
                .map(week -> resolveCurrentVersion(week.getUpdatedAt(), week.getSentAt()))
                .toList());
    }

    private Optional<LocalDateTime> resolveCustomCurrentVersion(Long userId, ParsedMessageId parsedMessageId) {
        List<FormatWeek> weeks = formatWeekRepository.findByStatusAndPlantPlantId(
                CascadaStatus.SENT,
                parsedMessageId.plantId()
        ).stream()
                .filter(week -> parsedMessageId.weekStartDate().equals(resolveFormatWeekStartDate(week)))
                .toList();

        if (weeks.isEmpty()) {
            return Optional.empty();
        }

        Set<String> allowedKeys = allowedRecipientKeys(userId, CascadaType.CUSTOM);
        List<FormatWeek> visibleWeeks = weeks.stream()
                .filter(week -> {
                    String shiftKey = week.getShift() == null ? null : week.getShift().getShiftId().toString();
                    if (shiftKey == null) {
                        return false;
                    }
                    return allowedKeys.contains(
                            week.getPlant().getPlantId() + "|" + resolveFormatWeekStartDate(week) + "|" + shiftKey
                    );
                })
                .toList();

        return resolveCurrentVersion(visibleWeeks.stream()
                .map(week -> resolveCurrentVersion(week.getUpdatedAt(), week.getSentAt()))
                .toList());
    }

    private Optional<LocalDateTime> resolveFlexsurCurrentVersion(Long userId, ParsedMessageId parsedMessageId) {
        List<FlexsurWeek> weeks = flexsurWeekRepository.findByStatusAndPlantPlantId(
                CascadaStatus.SENT,
                parsedMessageId.plantId()
        ).stream()
                .filter(week -> parsedMessageId.weekStartDate().equals(resolveWeekStartDate(week.getWeekDate())))
                .toList();

        if (weeks.isEmpty()) {
            return Optional.empty();
        }

        Set<String> allowedKeys = allowedRecipientKeys(userId, CascadaType.FLEXSUR);
        List<FlexsurWeek> visibleWeeks = weeks.stream()
                .filter(week -> allowedKeys.contains(
                        week.getPlant().getPlantId()
                                + "|"
                                + resolveWeekStartDate(week.getWeekDate())
                                + "|"
                                + recipientShiftKey(week.getShift() == null ? null : week.getShift().getShiftId())
                ))
                .toList();

        return resolveCurrentVersion(visibleWeeks.stream()
                .map(week -> resolveCurrentVersion(week.getUpdatedAt(), week.getSentAt()))
                .toList());
    }

    private Optional<LocalDateTime> resolveRegalCurrentVersion(Long userId, ParsedMessageId parsedMessageId) {
        List<RegalWeek> weeks = regalWeekRepository.findByStatusAndPlantPlantId(
                CascadaStatus.SENT,
                parsedMessageId.plantId()
        ).stream()
                .filter(week -> parsedMessageId.weekStartDate().equals(resolveWeekStartDate(week.getWeekDate())))
                .toList();

        if (weeks.isEmpty()) {
            return Optional.empty();
        }

        Set<String> allowedKeys = allowedRecipientKeys(userId, CascadaType.REGAL);
        List<RegalWeek> visibleWeeks = weeks.stream()
                .filter(week -> allowedKeys.contains(
                        week.getPlant().getPlantId()
                                + "|"
                                + resolveWeekStartDate(week.getWeekDate())
                                + "|"
                                + REGAL_RECIPIENT_SHIFT_KEY
                ))
                .toList();

        return resolveCurrentVersion(visibleWeeks.stream()
                .map(week -> resolveCurrentVersion(week.getUpdatedAt(), week.getSentAt()))
                .toList());
    }

    private Set<String> allowedRecipientKeys(Long userId, CascadaType cascadaType) {
        List<CascadaRecipient> recipients = cascadaRecipientRepository.findByRecipientUserIdAndCascadaType(
                userId,
                cascadaType
        );
        return recipients.stream()
                .map(recipient -> recipient.getPlant().getPlantId()
                        + "|"
                        + recipient.getWeekStartDate()
                        + "|"
                        + recipient.getShiftId())
                .collect(Collectors.toSet());
    }

    private Optional<ParsedMessageId> parseMessageId(String messageId) {
        Matcher matcher = MESSAGE_ID_PATTERN.matcher(messageId.trim().toLowerCase());
        if (!matcher.matches()) {
            return Optional.empty();
        }

        try {
            String type = matcher.group(1);
            Long plantId = Long.parseLong(matcher.group(2));
            LocalDate weekStartDate = LocalDate.parse(matcher.group(3));
            return Optional.of(new ParsedMessageId(type, plantId, weekStartDate));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private Optional<LocalDateTime> resolveCurrentVersion(List<LocalDateTime> versions) {
        return versions.stream()
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
    }

    private LocalDateTime resolveCurrentVersion(LocalDateTime updatedAt, LocalDateTime sentAt) {
        return updatedAt != null ? updatedAt : sentAt;
    }

    private LocalDate resolveFormatWeekStartDate(FormatWeek week) {
        if (week.getWeekStartDate() != null) {
            return week.getWeekStartDate();
        }
        return WeekMetadataResolver.resolvePreviousWeek(
                week.getWeekDate(),
                null,
                null,
                null
        ).getWeekStartDate();
    }

    private LocalDate resolveWeekStartDate(LocalDate weekDate) {
        return WeekMetadataResolver.resolve(weekDate, null, null, null).getWeekStartDate();
    }

    private String recipientShiftKey(Long shiftId) {
        return shiftId == null ? FLEXSUR_ALL_SHIFTS_RECIPIENT_KEY : shiftId.toString();
    }

    private record ParsedMessageId(String type, Long plantId, LocalDate weekStartDate) {
    }
}
