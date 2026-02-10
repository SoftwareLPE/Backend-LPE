package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekItemDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.CascadaWeek;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.CascadaWeekRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CascadaServiceImpl implements CascadaService {
    private static final Logger log = LoggerFactory.getLogger(CascadaServiceImpl.class);
    private final CascadaWeekRepository cascadaWeekRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final PlantRepository plantRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public CascadaServiceImpl(
            CascadaWeekRepository cascadaWeekRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            PlantRepository plantRepository,
            DriverRepository driverRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            ObjectMapper objectMapper
    ) {
        this.cascadaWeekRepository = cascadaWeekRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
        this.plantRepository = plantRepository;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey, String status) {
        CascadaWeek week = cascadaWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(
                        plantId,
                        weekStartDate,
                        shiftId
                )
                .orElse(null);

        List<CascadaRowDTO> rowDTOs = week == null
                ? List.of()
                : deserializeDayRows(week.getPayloadJson(), dayKey);

        if (week != null && status != null && !status.isBlank()) {
            if (!week.getStatus().name().equals(status)) {
                rowDTOs = List.of();
            }
        }

        CascadaResponseDTO response = new CascadaResponseDTO();
        response.setPlantId(plantId);
        response.setWeekStartDate(weekStartDate);
        response.setShiftId(shiftId);
        response.setDayKey(dayKey);
        response.setStatus(week == null ? status : week.getStatus().name());
        response.setRows(rowDTOs);
        return response;
    }

    @Override
    @Transactional
    public void saveCascada(CascadaSaveRequestDTO request) {
        log.info(
                "SAVE cascada: plantId={}, weekDate={}, shiftId={}, days={}",
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId(),
                request.getDays() == null ? 0 : request.getDays().size()
        );
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        if (request.getShiftId() == null || request.getShiftId().isBlank()) {
            throw new RuntimeException("shiftId is required");
        }
        if (request.getDays() == null || request.getDays().isEmpty()) {
            throw new RuntimeException("days is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        List<Long> driverIds = request.getDays().values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(list -> list.stream().map(CascadaRowDTO::getDriverId))
                .distinct()
                .toList();

        Map<Long, Driver> driversById = driverRepository.findAllById(driverIds).stream()
                .collect(Collectors.toMap(Driver::getDriverId, d -> d));

        for (Long driverId : driverIds) {
            if (!driversById.containsKey(driverId)) {
                throw new RuntimeException("Driver not found: " + driverId);
            }
        }

        Map<String, List<CascadaRowDTO>> normalizedDays = new java.util.HashMap<>();
        for (var entry : request.getDays().entrySet()) {
            String dayKey = entry.getKey();
            List<CascadaRowDTO> rows = entry.getValue() == null ? List.of() : entry.getValue();
            List<CascadaRowDTO> normalizedRows = rows.stream()
                    .map(this::normalizeRow)
                    .toList();
            normalizedDays.put(dayKey, normalizedRows);
            log.info("Saving payload_json for dayKey={}, rows={}", dayKey, normalizedRows);
        }

        upsertWeekPayload(
                plant,
                request.getWeekDate(),
                request.getShiftId(),
                normalizedDays
        );
    }

    @Override
    @Transactional
    public void sendCascada(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            Long userId,
            List<Long> recipientUserIds
    ) {
        CascadaWeek week = cascadaWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(
                        plantId,
                        weekStartDate,
                        shiftId
                )
                .orElse(null);

        if (week == null) {
            return;
        }

        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            throw new RuntimeException("recipientUserIds is required");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        week.setStatus(CascadaStatus.SENT);
        week.setSentAt(now);
        week.setSentByUserId(userId);
        week.setUpdatedAt(now);
        week.setUpdatedByUserId(userId);
        cascadaWeekRepository.save(week);

        cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
                plantId,
                weekStartDate,
                shiftId,
                dayKey
        );

        List<CascadaRecipient> recipients = new java.util.ArrayList<>();
        for (Long recipientUserId : recipientUserIds) {
            CascadaRecipient recipient = new CascadaRecipient();
            recipient.setPlant(week.getPlant());
            recipient.setWeekStartDate(weekStartDate);
            recipient.setShiftId(shiftId);
            recipient.setDayKey(dayKey);
            recipient.setRecipientUserId(recipientUserId);
            recipient.setSentAt(now);
            recipient.setSentByUserId(userId);
            recipients.add(recipient);
        }
        cascadaRecipientRepository.saveAll(recipients);

        // Publica el mensaje en tiempo real para cada destinatario.
        publishInboxMessages(week.getPlant().getPlantId(), weekStartDate, recipientUserIds);
    }

    @Override
    @Transactional
    public void deleteCascada(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            Long userId
    ) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        CascadaWeek week = cascadaWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(
                        plantId,
                        weekStartDate,
                        shiftId
                )
                .orElse(null);

        if (week == null) {
            return;
        }

        week.setStatus(CascadaStatus.DELETED);
        if (week.getSentAt() == null) {
            week.setSentAt(now);
        }
        if (week.getSentByUserId() == null) {
            week.setSentByUserId(userId);
        }
        week.setUpdatedAt(now);
        week.setUpdatedByUserId(userId);
        cascadaWeekRepository.save(week);

        publishInboxMessagesForStatus(
                CascadaStatus.DELETED.name(),
                plantId,
                weekStartDate,
                recipientUserIdsFor(plantId, weekStartDate, shiftId, dayKey)
        );
    }

    @Override
    @Transactional
    public void updateCascadaStatus(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey,
            String status,
            Long userId,
            List<Long> recipientUserIds
    ) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekStartDate == null) {
            throw new RuntimeException("weekStartDate is required");
        }
        if (shiftId == null || shiftId.isBlank()) {
            throw new RuntimeException("shiftId is required");
        }
        if (dayKey != null && dayKey.isBlank()) {
            dayKey = null;
        }
        if (status == null || status.isBlank()) {
            throw new RuntimeException("status is required");
        }

        CascadaStatus targetStatus;
        try {
            targetStatus = CascadaStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status);
        }

        if (targetStatus != CascadaStatus.SENT && targetStatus != CascadaStatus.DELETED) {
            throw new RuntimeException("status must be SENT or DELETED");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sentAtToUse = now;
        Long sentByToUse = userId;

        CascadaWeek existingWeek = cascadaWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(
                        plantId,
                        weekStartDate,
                        shiftId
                )
                .orElse(null);

        if (existingWeek != null) {
            if (existingWeek.getSentByUserId() != null) {
                sentByToUse = existingWeek.getSentByUserId();
            }
            if (existingWeek.getSentAt() != null) {
                sentAtToUse = existingWeek.getSentAt();
            }
        }

        int updated = cascadaWeekRepository.updateStatusByPlantAndWeekAndShift(
                plantId,
                weekStartDate,
                shiftId,
                targetStatus,
                sentAtToUse,
                sentByToUse,
                now,
                userId
        );

        if (updated == 0) {
            return;
        }

        if (targetStatus == CascadaStatus.SENT) {
            if (recipientUserIds == null || recipientUserIds.isEmpty()) {
                throw new RuntimeException("recipientUserIds is required");
            }

            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));

            if (dayKey == null) {
                cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftId(
                        plantId,
                        weekStartDate,
                        shiftId
                );
            } else {
                cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
                        plantId,
                        weekStartDate,
                        shiftId,
                        dayKey
                );
            }

            List<CascadaRecipient> recipients = new ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(weekStartDate);
                recipient.setShiftId(shiftId);
                recipient.setDayKey(dayKey);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);

            publishInboxMessages(plantId, weekStartDate, recipientUserIds);
            return;
        }

        publishInboxMessagesForStatus(
                CascadaStatus.DELETED.name(),
                plantId,
                weekStartDate,
                recipientUserIdsFor(plantId, weekStartDate, shiftId, dayKey)
        );
    }

    @Override
    public CascadaWeekResponseDTO getWeekCascadas(Long plantId, LocalDate weekStartDate, String status) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekStartDate == null) {
            throw new RuntimeException("weekStartDate is required");
        }
        if (status == null || status.isBlank()) {
            throw new RuntimeException("status is required");
        }

        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status);
        }

        // Trae todos los dias de la semana en una sola llamada.
        List<CascadaWeek> weeks = cascadaWeekRepository
                .findByPlantPlantIdAndWeekStartDateAndStatus(plantId, weekStartDate, cascadaStatus);

        List<CascadaWeekItemDTO> items = new ArrayList<>();
        for (CascadaWeek week : weeks) {
            java.util.Map<String, List<CascadaRowDTO>> days = deserializeDays(week.getPayloadJson());
            for (var entry : days.entrySet()) {
                String dayKey = entry.getKey();
                List<CascadaRowDTO> rowDTOs = entry.getValue();
                Long shiftIdValue = null;
                if (week.getShiftId() != null) {
                    try {
                        shiftIdValue = Long.parseLong(week.getShiftId());
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Invalid shiftId stored: " + week.getShiftId());
                    }
                }
                items.add(new CascadaWeekItemDTO(shiftIdValue, dayKey, rowDTOs));
            }
        }

        return new CascadaWeekResponseDTO(plantId, weekStartDate, status, items);
    }

    @Override
    public List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> getCascadaSummaries(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            Long recipientUserId
    ) {
        // Summary for the inbox listing (filterable by plant/week/status).
        String effectiveStatus = (status == null || status.isBlank()) ? CascadaStatus.SENT.name() : status;
        List<CascadaWeek> weeks = cascadaWeekRepository.findWeeksForSummary(
                CascadaStatus.valueOf(effectiveStatus),
                plantId,
                weekStartDate,
                recipientUserId
        );

        java.util.Map<String, List<CascadaWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + week.getWeekStartDate()
                ));

        List<com.example.backend_sistema_LPE.dto.CascadaSummaryDTO> summaries = new java.util.ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<CascadaWeek> groupWeeks = entry.getValue();
            CascadaWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(CascadaWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            java.util.Set<String> shiftIds = groupWeeks.stream()
                    .map(CascadaWeek::getShiftId)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Set<String> dayKeys = groupWeeks.stream()
                    .flatMap(week -> deserializeDays(week.getPayloadJson()).keySet().stream())
                    .collect(java.util.stream.Collectors.toSet());

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new com.example.backend_sistema_LPE.dto.CascadaSummaryDTO(
                    latest.getCascadaWeekId(),
                    latest.getPlant().getPlantId() + "-" + latest.getWeekStartDate(),
                    latest.getPlant().getPlantId(),
                    latest.getPlant().getPlantName(),
                    latest.getPlant().getCompany().getCompanyId(),
                    latest.getPlant().getCompany().getCompanyName(),
                    sentBy,
                    latest.getWeekStartDate(),
                    shiftIds,
                    dayKeys,
                    latest.getSentAt()
            ));
        }

        summaries.sort(java.util.Comparator.comparing(
                com.example.backend_sistema_LPE.dto.CascadaSummaryDTO::getSentAt,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ).reversed());

        return summaries;
    }

    private String serializeDays(java.util.Map<String, List<CascadaRowDTO>> days) {
        try {
            return objectMapper.writeValueAsString(java.util.Map.of("days", days));
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize cascada payload");
        }
    }

    private CascadaRowDTO normalizeRow(CascadaRowDTO row) {
        CascadaRowDTO normalized = new CascadaRowDTO();
        normalized.setDriverId(row.getDriverId());
        normalized.setE(normalizeValue(row.getE()));
        normalized.setS(normalizeValue(row.getS()));
        normalized.setEte(normalizeValue(row.getEte()));
        normalized.setSte(normalizeValue(row.getSte()));
        return normalized;
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value;
    }

    private void upsertWeekPayload(
            Plant plant,
            LocalDate weekDate,
            String shiftId,
            Map<String, List<CascadaRowDTO>> daysToMerge
    ) {
        for (int attempt = 0; attempt < 2; attempt++) {
            CascadaWeek week = cascadaWeekRepository
                    .findByPlantPlantIdAndWeekStartDateAndShiftId(
                            plant.getPlantId(),
                            weekDate,
                            shiftId
                    )
                    .orElse(null);

            String payloadJson;
            if (week == null || week.getPayloadJson() == null || week.getPayloadJson().isBlank()) {
                payloadJson = serializeDays(daysToMerge);
            } else {
                java.util.Map<String, List<CascadaRowDTO>> days = deserializeDays(week.getPayloadJson());
                days.putAll(daysToMerge);
                payloadJson = serializeDays(days);
            }

            LocalDateTime now = LocalDateTime.now();
            if (week == null) {
                week = new CascadaWeek();
                week.setPlant(plant);
                week.setWeekStartDate(weekDate);
                week.setShiftId(shiftId);
            }

            week.setPayloadJson(payloadJson);
            week.setStatus(CascadaStatus.DRAFT);
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(null);
            week.setSentAt(null);
            week.setSentByUserId(null);

            try {
                cascadaWeekRepository.save(week);
                return;
            } catch (DataIntegrityViolationException ex) {
                // Probable carrera: otro request insertó la misma fila (plant+week+shift).
                if (attempt == 1) {
                    throw ex;
                }
            }
        }
    }

    private java.util.Map<String, List<CascadaRowDTO>> deserializeDays(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(payloadJson);
            com.fasterxml.jackson.databind.JsonNode daysNode = node.get("days");
            if (daysNode == null || !daysNode.isObject()) {
                return new java.util.HashMap<>();
            }

            java.util.Map<String, List<CascadaRowDTO>> days = new java.util.HashMap<>();
            java.util.Iterator<java.util.Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>> fields = daysNode.fields();
            while (fields.hasNext()) {
                var field = fields.next();
                String dayKey = field.getKey();
                com.fasterxml.jackson.databind.JsonNode rowsNode = field.getValue();
                if (rowsNode == null || !rowsNode.isArray()) {
                    days.put(dayKey, List.of());
                    continue;
                }
                List<CascadaRowDTO> rows = new ArrayList<>();
                for (com.fasterxml.jackson.databind.JsonNode item : rowsNode) {
                    CascadaRowDTO row = objectMapper.treeToValue(item, CascadaRowDTO.class);
                    rows.add(row);
                }
                days.put(dayKey, rows);
            }
            return days;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to deserialize cascada payload");
        }
    }

    private List<CascadaRowDTO> deserializeDayRows(String payloadJson, String dayKey) {
        if (dayKey == null || dayKey.isBlank()) {
            return List.of();
        }
        java.util.Map<String, List<CascadaRowDTO>> days = deserializeDays(payloadJson);
        return days.getOrDefault(dayKey, List.of());
    }

    private void publishInboxMessages(
            Long plantId,
            LocalDate weekStartDate,
            List<Long> recipientUserIds
    ) {
        publishInboxMessagesForStatus(CascadaStatus.SENT.name(), plantId, weekStartDate, recipientUserIds);
    }

    private void publishInboxMessagesForStatus(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            List<Long> recipientUserIds
    ) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return;
        }

        for (Long recipientUserId : recipientUserIds) {
            List<CascadaSummaryDTO> summaries = getCascadaSummaries(
                    status,
                    plantId,
                    weekStartDate,
                    recipientUserId
            );
            if (summaries.isEmpty()) {
                continue;
            }

            CascadaSummaryDTO summary = summaries.get(0);
            String plantName = summary.getPlantName();
            String companyName = summary.getCompanyName();
            String weekStart = summary.getWeekDate().toString();
            LocalDateTime sentAt = summary.getSentAt();
            String sentAtValue = sentAt != null ? sentAt.toString() : null;

            String title = plantName;
            String subtitle = "Semana " + weekStart;
            String fileName = "Cascada_" + plantName + "_" + weekStart + ".xlsx";
            String sheetTitle = plantName;

            InboxMessageDTO payload = new InboxMessageDTO(
                    summary.getId(),
                    summary.getCascadaId(),
                    title,
                    subtitle,
                    sentAtValue,
                    sentAtValue,
                    fileName,
                    sheetTitle,
                    companyName,
                    summary.getSentBy(),
                    summary.getPlantId(),
                    weekStart,
                    weekStart,
                    summary.getShiftIds().stream().toList(),
                    summary.getDayKeys().stream().toList(),
                    status
            );

            // Canal por destinatario: el cliente debe suscribirse a /topic/inbox/{userId}
            messagingTemplate.convertAndSend(
                    "/topic/inbox/" + recipientUserId,
                    payload
            );
        }
    }

    private List<Long> recipientUserIdsFor(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey
    ) {
        return cascadaRecipientRepository.findRecipientUserIds(
                plantId,
                weekStartDate,
                shiftId,
                dayKey
        );
    }
}
