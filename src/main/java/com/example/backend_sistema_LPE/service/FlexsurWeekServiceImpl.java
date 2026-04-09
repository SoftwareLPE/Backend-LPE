package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurDetailDTO;
import com.example.backend_sistema_LPE.dto.FlexsurManualRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaColumnDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaDayDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSchemaSectionDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.enums.CascadaType;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.FlexsurDetail;
import com.example.backend_sistema_LPE.model.FlexsurManualRow;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.FlexsurWeek;
import com.example.backend_sistema_LPE.model.FlexsurWeekTotalsSnapshot;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.FlexsurManualRowRepository;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.FlexsurWeekRepository;
import com.example.backend_sistema_LPE.repository.FlexsurWeekTotalsSnapshotRepository;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class FlexsurWeekServiceImpl implements FlexsurWeekService {
    private static final String FLEXSUR_ALL_SHIFTS_RECIPIENT_KEY = "FLEXSUR";

    private final FlexsurWeekRepository flexsurWeekRepository;
    private final FlexsurManualRowRepository flexsurManualRowRepository;
    private final FlexsurServiceRepository flexsurServiceRepository;
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;
    private final FlexsurWeekTotalsSnapshotRepository flexsurWeekTotalsSnapshotRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FlexsurWeekServiceImpl(
            FlexsurWeekRepository flexsurWeekRepository,
            FlexsurManualRowRepository flexsurManualRowRepository,
            FlexsurServiceRepository flexsurServiceRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository,
            FlexsurWeekTotalsSnapshotRepository flexsurWeekTotalsSnapshotRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.flexsurWeekRepository = flexsurWeekRepository;
        this.flexsurManualRowRepository = flexsurManualRowRepository;
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
        this.flexsurWeekTotalsSnapshotRepository = flexsurWeekTotalsSnapshotRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public FlexsurWeekSchemaDTO getFlexsurWeekSchema(Long plantId, LocalDate weekDate) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        LocalDate weekStart = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<FlexsurWeekSchemaColumnDTO> dayColumns = List.of(
                new FlexsurWeekSchemaColumnDTO("trips", "VIAJES", 1),
                new FlexsurWeekSchemaColumnDTO("columnExtra", "", 2),
                new FlexsurWeekSchemaColumnDTO("total", "TOTAL", 3)
        );

        List<FlexsurWeekSchemaDayDTO> days = new ArrayList<>();
        for (int offset = 0; offset < 7; offset++) {
            LocalDate serviceDate = weekStart.plusDays(offset);
            days.add(new FlexsurWeekSchemaDayDTO(
                    mapDayKey(serviceDate),
                    formatDayLabel(serviceDate),
                    serviceDate,
                    dayColumns
            ));
        }

        List<FlexsurWeekSchemaSectionDTO> sections = List.of(
                new FlexsurWeekSchemaSectionDTO("top", days.subList(0, Math.min(3, days.size()))),
                new FlexsurWeekSchemaSectionDTO("middle", days.size() > 3 ? days.subList(3, Math.min(6, days.size())) : List.of()),
                new FlexsurWeekSchemaSectionDTO("bottom", days.size() > 6 ? days.subList(6, days.size()) : List.of())
        );

        return new FlexsurWeekSchemaDTO(
                plantId,
                weekStart,
                List.of("SERVICIOS"),
                days,
                sections,
                "TOTAL SEMANAL"
        );
    }

    @Override
    public FlexsurWeekResponseDTO getFlexsurWeek(Long plantId, LocalDate weekDate, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        resolveShiftIfPresent(plantId, shiftId);

        List<FlexsurWeek> weeks = findWeeks(plantId, weekDate, shiftId);
        List<FlexsurManualRow> manualRows = findManualRows(plantId, weekDate, shiftId);

        List<FlexsurWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .toList();
        List<FlexsurWeekRowDTO> baseRows = new ArrayList<>(buildCatalogRows(plantId, shiftId));
        baseRows.addAll(buildManualRows(manualRows));
        List<FlexsurWeekRowDTO> rows = mergeRows(baseRows, savedRows);
        enrichRowTotals(rows);

        FlexsurWeekTotalsDTO totals = buildTotals(rows);
        applyPersistedTotals(totals, findTotalsSnapshot(plantId, weekDate, shiftId).orElse(null));
        String status = resolveStatus(weeks);
        return new FlexsurWeekResponseDTO(plantId, weekDate, shiftId, status, rows, totals);
    }

    @Override
    @Transactional
    public void saveFlexsurWeek(FlexsurWeekSaveRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        Shift shift = resolveShiftIfPresent(request.getPlantId(), request.getShiftId());
        List<FlexsurManualRow> manualRows = findManualRows(
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId()
        );

        deleteWeeks(request.getPlantId(), request.getWeekDate(), request.getShiftId());
        deleteTotalsSnapshot(request.getPlantId(), request.getWeekDate(), request.getShiftId());

        List<FlexsurWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        List<FlexsurWeek> persistedWeeks = new ArrayList<>();
        for (FlexsurWeekRowDTO row : rows) {
            FlexsurManualRow manualRow = resolveManualRow(row, manualRows, request.getShiftId());
            if (isManualRow(row) && manualRow == null) {
                throw new RuntimeException("Manual row not found: " + row.getManualFlexsurRowId());
            }
            String serviceName = manualRow != null ? manualRow.getServiceName() : trimToNull(row.getServiceName());
            if (serviceName == null) {
                throw new RuntimeException("serviceName is required");
            }

            FlexsurWeek week = new FlexsurWeek();
            week.setPlant(plant);
            week.setWeekDate(request.getWeekDate());
            week.setShift(shift);
            week.setManualRow(manualRow);
            week.setServiceName(serviceName);
            week.setStatus(CascadaStatus.DRAFT);

            LocalDateTime now = LocalDateTime.now();
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(userId);

            List<FlexsurDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            List<FlexsurDetail> detailEntities = new ArrayList<>();
            for (FlexsurDetailDTO detailDTO : details) {
                if (detailDTO.getServiceDate() == null) {
                    throw new RuntimeException("serviceDate is required");
                }
                FlexsurDetail detail = new FlexsurDetail();
                detail.setFlexsurWeek(week);
                detail.setServiceDate(detailDTO.getServiceDate());
                int trips = detailDTO.getTrips() == null ? 0 : detailDTO.getTrips();
                int extraColumn = detailDTO.getExtraColumn() == null ? 0 : detailDTO.getExtraColumn();
                int total = detailDTO.getTotal() == null ? trips + extraColumn : detailDTO.getTotal();
                detail.setTrips(trips);
                detail.setExtraColumn(extraColumn);
                detail.setTotal(total);
                detailEntities.add(detail);
            }
            week.setDetails(detailEntities);
            persistedWeeks.add(flexsurWeekRepository.save(week));
        }

        saveTotalsSnapshot(plant, shift, request.getWeekDate(), persistedWeeks, userId);
    }

    @Override
    @Transactional
    public FlexsurManualRowDTO createManualRow(CreateFlexsurManualRowRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        String serviceName = trimToNull(request.getServiceName());
        if (serviceName == null) {
            throw new RuntimeException("serviceName is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        Shift shift = resolveShiftIfPresent(request.getPlantId(), request.getShiftId());
        List<FlexsurManualRow> existing = findManualRows(
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId()
        );

        int nextSortOrder = existing.stream()
                .map(FlexsurManualRow::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(existing.size());

        FlexsurManualRow manualRow = new FlexsurManualRow();
        manualRow.setPlant(plant);
        manualRow.setWeekDate(request.getWeekDate());
        manualRow.setShift(shift);
        manualRow.setServiceName(serviceName);
        manualRow.setSortOrder(request.getSortOrder() == null ? nextSortOrder : request.getSortOrder());
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        return toManualRowDTO(flexsurManualRowRepository.save(manualRow));
    }

    @Override
    @Transactional
    public FlexsurManualRowDTO updateManualRow(
            Long manualFlexsurRowId,
            UpdateFlexsurManualRowRequestDTO request,
            Long userId
    ) {
        if (manualFlexsurRowId == null) {
            throw new RuntimeException("manualFlexsurRowId is required");
        }

        FlexsurManualRow manualRow = flexsurManualRowRepository.findById(manualFlexsurRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));

        if (request.getServiceName() != null) {
            String serviceName = trimToNull(request.getServiceName());
            if (serviceName == null) {
                throw new RuntimeException("serviceName is required");
            }
            manualRow.setServiceName(serviceName);
        }
        if (request.getSortOrder() != null) {
            manualRow.setSortOrder(request.getSortOrder());
        }
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        return toManualRowDTO(flexsurManualRowRepository.save(manualRow));
    }

    @Override
    @Transactional
    public void deleteManualRow(Long manualFlexsurRowId) {
        if (manualFlexsurRowId == null) {
            throw new RuntimeException("manualFlexsurRowId is required");
        }

        FlexsurManualRow manualRow = flexsurManualRowRepository.findById(manualFlexsurRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));
        deleteManualRowsAndCounts(List.of(manualRow));
    }

    @Override
    @Transactional
    public void updateFlexsurStatus(Long plantId, LocalDate weekDate, Long shiftId, String status, Long userId) {
        updateFlexsurStatus(plantId, weekDate, shiftId, status, userId, null);
    }

    @Override
    @Transactional
    public void updateFlexsurStatus(
            Long plantId,
            LocalDate weekDate,
            Long shiftId,
            String status,
            Long userId,
            List<Long> recipientUserIds
    ) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
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

        resolveShiftIfPresent(plantId, shiftId);
        List<FlexsurWeek> weeks = findWeeks(plantId, weekDate, shiftId);
        if (weeks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (targetStatus == CascadaStatus.SENT && (recipientUserIds == null || recipientUserIds.stream().filter(java.util.Objects::nonNull).toList().isEmpty())) {
            throw new RuntimeException("recipientUserIds is required");
        }
        for (FlexsurWeek week : weeks) {
            if (targetStatus == CascadaStatus.SENT) {
                if (week.getSentAt() == null) {
                    week.setSentAt(now);
                }
                if (week.getSentByUserId() == null) {
                    week.setSentByUserId(userId);
                }
            }
            week.setStatus(targetStatus);
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(userId);
        }
        flexsurWeekRepository.saveAll(weeks);

        LocalDate effectiveWeekStartDate = resolveWeekStartDate(weeks.get(0));
        String recipientShiftKey = recipientShiftKey(shiftId);
        if (targetStatus == CascadaStatus.SENT) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));
            cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndCascadaType(
                    plantId,
                    effectiveWeekStartDate,
                    recipientShiftKey,
                    CascadaType.FLEXSUR
            );
            List<CascadaRecipient> recipients = new ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                if (recipientUserId == null) {
                    continue;
                }
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(effectiveWeekStartDate);
                recipient.setShiftId(recipientShiftKey);
                recipient.setDayKey(null);
                recipient.setCascadaType(CascadaType.FLEXSUR);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);
            publishFlexsurInboxMessagesAfterCommit(
                    targetStatus.name(),
                    plantId,
                    effectiveWeekStartDate,
                    recipientUserIds
            );
            return;
        }

        if (targetStatus == CascadaStatus.DELETED) {
            publishFlexsurInboxMessagesAfterCommit(
                    targetStatus.name(),
                    plantId,
                    effectiveWeekStartDate,
                    recipientUserIdsForFlexsur(plantId, effectiveWeekStartDate, shiftId)
            );
        }
    }

    @Override
    public List<CascadaSummaryDTO> getFlexsurSummaries(String status, Long plantId, LocalDate weekDate, Long recipientUserId) {
        String effectiveStatus = (status == null || status.isBlank()) ? CascadaStatus.SENT.name() : status;
        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(effectiveStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + effectiveStatus);
        }

        List<FlexsurWeek> weeks = flexsurWeekRepository.findByStatus(cascadaStatus);
        if (plantId != null) {
            weeks = weeks.stream()
                    .filter(week -> week.getPlant().getPlantId().equals(plantId))
                    .toList();
        }
        if (weekDate != null) {
            LocalDate effectiveRequestedWeekStartDate = WeekMetadataResolver.resolve(weekDate, null, null, null).getWeekStartDate();
            weeks = weeks.stream()
                    .filter(week -> resolveWeekStartDate(week).equals(effectiveRequestedWeekStartDate))
                    .toList();
        }

        if (recipientUserId != null) {
            List<CascadaRecipient> recipients = cascadaRecipientRepository
                    .findByRecipientUserIdAndCascadaType(recipientUserId, CascadaType.FLEXSUR);
            java.util.Set<String> allowedKeys = recipients.stream()
                    .map(r -> r.getPlant().getPlantId() + "|" + r.getWeekStartDate() + "|" + r.getShiftId())
                    .collect(java.util.stream.Collectors.toSet());

            weeks = weeks.stream()
                    .filter(week -> allowedKeys.contains(
                            week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week) + "|" + recipientShiftKey(week.getShift() == null ? null : week.getShift().getShiftId())
                    ))
                    .toList();
        }

        java.util.Map<String, List<FlexsurWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week)
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<FlexsurWeek> groupWeeks = entry.getValue();
            FlexsurWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(FlexsurWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            LocalDate effectiveWeekStartDate = resolveWeekStartDate(latest);
            java.util.Set<String> shiftIds = groupWeeks.stream()
                    .map(week -> week.getShift() == null ? null : week.getShift().getShiftId().toString())
                    .filter(java.util.Objects::nonNull)
                    .sorted(this::compareShiftIds)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            java.util.Set<String> dayKeys = groupWeeks.stream()
                    .flatMap(week -> week.getDetails().stream().map(detail -> mapDayKey(detail.getServiceDate())))
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = UserDisplayNameResolver.resolve(user);
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getFlexsurWeekId(),
                    stableCascadaId(latest.getPlant().getPlantId(), effectiveWeekStartDate),
                    latest.getPlant().getPlantId(),
                    latest.getPlant().getPlantName(),
                    latest.getPlant().getCompany().getCompanyId(),
                    latest.getPlant().getCompany().getCompanyName(),
                    sentBy,
                    effectiveWeekStartDate,
                    effectiveWeekStartDate,
                    resolveWeekEndDate(latest),
                    resolveWeekNumber(latest),
                    shiftIds,
                    orderDayKeys(dayKeys),
                    latest.getSentAt()
            ));
        }

        summaries.sort(java.util.Comparator.comparing(
                CascadaSummaryDTO::getSentAt,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ).reversed());

        return summaries;
    }

    private FlexsurWeekRowDTO toRowDTO(FlexsurWeek week) {
        List<FlexsurDetailDTO> details = week.getDetails() == null
                ? List.of()
                : week.getDetails().stream()
                        .map(detail -> new FlexsurDetailDTO(
                                detail.getServiceDate(),
                                detail.getTrips(),
                                detail.getExtraColumn(),
                                detail.getTotal()
                        ))
                        .toList();

        return new FlexsurWeekRowDTO(
                week.getFlexsurWeekId(),
                week.getManualRow() == null ? null : week.getManualRow().getManualFlexsurRowId(),
                week.getManualRow() == null ? week.getServiceName() : week.getManualRow().getServiceName(),
                details,
                null
        );
    }

    private List<FlexsurWeekRowDTO> buildManualRows(List<FlexsurManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return List.of();
        }
        return manualRows.stream()
                .map(row -> new FlexsurWeekRowDTO(
                        null,
                        row.getManualFlexsurRowId(),
                        row.getServiceName(),
                        List.of(),
                        null
                ))
                .toList();
    }

    private List<FlexsurWeekRowDTO> buildCatalogRows(Long plantId, Long shiftId) {
        List<FlexsurService> services = shiftId == null
                ? flexsurServiceRepository.findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId)
                : flexsurServiceRepository.findByPlantPlantIdAndShiftShiftIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId, shiftId);
        if (services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .map(service -> new FlexsurWeekRowDTO(
                        null,
                        null,
                        service.getServiceName(),
                        List.of(),
                        null
                ))
                .toList();
    }

    private List<FlexsurWeekRowDTO> mergeRows(List<FlexsurWeekRowDTO> baseRows, List<FlexsurWeekRowDTO> savedRows) {
        if (baseRows == null || baseRows.isEmpty()) {
            return savedRows;
        }

        Map<String, FlexsurWeekRowDTO> baseByKey = new LinkedHashMap<>();
        for (FlexsurWeekRowDTO row : baseRows) {
            baseByKey.put(rowKey(row), row);
        }

        List<FlexsurWeekRowDTO> merged = new ArrayList<>(baseRows);
        for (FlexsurWeekRowDTO saved : savedRows) {
            String key = rowKey(saved);
            FlexsurWeekRowDTO base = baseByKey.get(key);
            if (base == null) {
                merged.add(saved);
                continue;
            }
            mergeRow(base, saved);
        }
        return merged;
    }

    private String rowKey(FlexsurWeekRowDTO row) {
        if (row.getManualFlexsurRowId() != null) {
            return "M:" + row.getManualFlexsurRowId();
        }
        return "S:" + normalizeKey(row.getServiceName());
    }

    private void mergeRow(FlexsurWeekRowDTO base, FlexsurWeekRowDTO saved) {
        if (base.getFlexsurWeekId() == null && saved.getFlexsurWeekId() != null) {
            base.setFlexsurWeekId(saved.getFlexsurWeekId());
        }
        if (base.getManualFlexsurRowId() == null && saved.getManualFlexsurRowId() != null) {
            base.setManualFlexsurRowId(saved.getManualFlexsurRowId());
        }
        if (base.getServiceName() == null && saved.getServiceName() != null) {
            base.setServiceName(saved.getServiceName());
        }
        if (saved.getDetails() != null && !saved.getDetails().isEmpty()) {
            base.setDetails(saved.getDetails());
        }
    }

    private FlexsurWeekTotalsDTO buildTotals(List<FlexsurWeekRowDTO> rows) {
        Map<String, Integer> byDay = new LinkedHashMap<>();
        Map<String, Integer> byService = new HashMap<>();
        Map<String, Integer> byColumn = new LinkedHashMap<>();
        byColumn.put("trips", 0);
        byColumn.put("extraColumn", 0);
        byColumn.put("total", 0);

        int weekTotal = 0;

        for (FlexsurWeekRowDTO row : rows) {
            String serviceName = row.getServiceName();
            List<FlexsurDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            for (FlexsurDetailDTO detail : details) {
                String dayKey = detail.getServiceDate() == null ? null : detail.getServiceDate().toString();
                int trips = detail.getTrips() == null ? 0 : detail.getTrips();
                int extra = detail.getExtraColumn() == null ? 0 : detail.getExtraColumn();
                int total = detail.getTotal() == null ? trips + extra : detail.getTotal();

                if (dayKey != null) {
                    addStringCount(byDay, dayKey, total);
                }
                addStringCount(byService, serviceName, total);
                addStringCount(byColumn, "trips", trips);
                addStringCount(byColumn, "extraColumn", extra);
                addStringCount(byColumn, "total", total);
                weekTotal += total;
            }
        }

        return new FlexsurWeekTotalsDTO(byDay, byService, byColumn, weekTotal);
    }

    private Optional<FlexsurWeekTotalsSnapshot> findTotalsSnapshot(Long plantId, LocalDate weekDate, Long shiftId) {
        return shiftId == null
                ? flexsurWeekTotalsSnapshotRepository.findByPlantPlantIdAndWeekDateAndShiftIsNull(plantId, weekDate)
                : flexsurWeekTotalsSnapshotRepository.findByPlantPlantIdAndWeekDateAndShiftShiftId(plantId, weekDate, shiftId);
    }

    private void deleteTotalsSnapshot(Long plantId, LocalDate weekDate, Long shiftId) {
        findTotalsSnapshot(plantId, weekDate, shiftId)
                .ifPresent(flexsurWeekTotalsSnapshotRepository::delete);
    }

    private void saveTotalsSnapshot(
            Plant plant,
            Shift shift,
            LocalDate weekDate,
            List<FlexsurWeek> persistedWeeks,
            Long userId
    ) {
        FlexsurWeekTotalsSnapshot snapshot = new FlexsurWeekTotalsSnapshot();
        snapshot.setPlant(plant);
        snapshot.setWeekDate(weekDate);
        snapshot.setShift(shift);
        snapshot.setByDay(buildPersistedByDayTotals(persistedWeeks));
        snapshot.setWeekTotal(snapshot.getByDay().values().stream().mapToInt(Integer::intValue).sum());
        snapshot.setUpdatedAt(LocalDateTime.now());
        snapshot.setUpdatedByUserId(userId);
        flexsurWeekTotalsSnapshotRepository.save(snapshot);
    }

    private Map<LocalDate, Integer> buildPersistedByDayTotals(List<FlexsurWeek> persistedWeeks) {
        Map<LocalDate, Integer> byDay = new LinkedHashMap<>();
        if (persistedWeeks == null || persistedWeeks.isEmpty()) {
            return byDay;
        }
        for (FlexsurWeek week : persistedWeeks) {
            List<FlexsurDetail> details = week.getDetails() == null ? List.of() : week.getDetails();
            for (FlexsurDetail detail : details) {
                if (detail.getServiceDate() == null) {
                    continue;
                }
                int total = detail.getTotal() == null ? 0 : detail.getTotal();
                byDay.merge(detail.getServiceDate(), total, Integer::sum);
            }
        }
        return byDay;
    }

    private void applyPersistedTotals(FlexsurWeekTotalsDTO totals, FlexsurWeekTotalsSnapshot snapshot) {
        if (totals == null || snapshot == null) {
            return;
        }
        Map<String, Integer> byDay = new LinkedHashMap<>();
        Map<LocalDate, Integer> persistedByDay = snapshot.getByDay() == null ? Map.of() : snapshot.getByDay();
        persistedByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> byDay.put(entry.getKey().toString(), entry.getValue()));
        totals.setByDay(byDay);
        totals.setWeekTotal(snapshot.getWeekTotal() == null ? 0 : snapshot.getWeekTotal());
    }

    private void enrichRowTotals(List<FlexsurWeekRowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (FlexsurWeekRowDTO row : rows) {
            List<FlexsurDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            int rowTotal = details.stream()
                    .mapToInt(detail -> detail.getTotal() == null
                            ? (detail.getTrips() == null ? 0 : detail.getTrips()) + (detail.getExtraColumn() == null ? 0 : detail.getExtraColumn())
                            : detail.getTotal())
                    .sum();
            row.setRowTotal(rowTotal);
        }
    }

    private String resolveStatus(List<FlexsurWeek> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return null;
        }
        CascadaStatus status = weeks.get(0).getStatus();
        return status == null ? null : status.name();
    }

    private FlexsurManualRow resolveManualRow(FlexsurWeekRowDTO row, List<FlexsurManualRow> manualRows, Long shiftId) {
        if (row == null || row.getManualFlexsurRowId() == null) {
            return null;
        }
        if (manualRows != null && !manualRows.isEmpty()) {
            FlexsurManualRow persisted = manualRows.stream()
                    .filter(manualRow -> row.getManualFlexsurRowId().equals(manualRow.getManualFlexsurRowId()))
                    .findFirst()
                    .orElse(null);
            if (persisted != null) {
                return persisted;
            }
        }
        FlexsurManualRow persisted = flexsurManualRowRepository.findById(row.getManualFlexsurRowId()).orElse(null);
        if (persisted == null) {
            return null;
        }
        Long persistedShiftId = persisted.getShift() == null ? null : persisted.getShift().getShiftId();
        if (!java.util.Objects.equals(persistedShiftId, shiftId)) {
            return null;
        }
        return persisted;
    }

    private boolean isManualRow(FlexsurWeekRowDTO row) {
        return row != null && row.getManualFlexsurRowId() != null;
    }

    private FlexsurManualRowDTO toManualRowDTO(FlexsurManualRow row) {
        return new FlexsurManualRowDTO(
                row.getManualFlexsurRowId(),
                row.getShift() == null ? null : row.getShift().getShiftId(),
                row.getServiceName(),
                row.getSortOrder()
        );
    }

    private void deleteManualRowsAndCounts(List<FlexsurManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return;
        }
        List<Long> manualRowIds = manualRows.stream()
                .map(FlexsurManualRow::getManualFlexsurRowId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!manualRowIds.isEmpty()) {
            List<FlexsurWeek> weeks = flexsurWeekRepository.findByManualRowManualFlexsurRowIdIn(manualRowIds);
            if (!weeks.isEmpty()) {
                flexsurWeekRepository.deleteAll(weeks);
            }
        }
        flexsurManualRowRepository.deleteAll(manualRows);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeKey(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String mapDayKey(LocalDate date) {
        if (date == null) {
            return null;
        }
        return switch (date.getDayOfWeek()) {
            case MONDAY -> "lun";
            case TUESDAY -> "mar";
            case WEDNESDAY -> "mie";
            case THURSDAY -> "jue";
            case FRIDAY -> "vie";
            case SATURDAY -> "sab";
            case SUNDAY -> "dom";
        };
    }

    private String formatDayLabel(LocalDate date) {
        if (date == null) {
            return "";
        }
        String dayName = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "MX"))
                .toUpperCase(Locale.ROOT);
        return dayName + " " + date.getDayOfMonth();
    }

    private void addStringCount(Map<String, Integer> totals, String key, int count) {
        if (key == null || key.isBlank() || count == 0) {
            return;
        }
        totals.put(key, totals.getOrDefault(key, 0) + count);
    }

    private void publishFlexsurInboxMessagesForStatus(
            String status,
            Long plantId,
            LocalDate weekDate,
            List<Long> recipientUserIds
    ) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return;
        }
        for (Long recipientUserId : recipientUserIds) {
            if (recipientUserId == null) {
                continue;
            }
            List<CascadaSummaryDTO> summaries = getFlexsurSummaries(status, plantId, weekDate, recipientUserId);
            if (summaries.isEmpty()) {
                continue;
            }

            CascadaSummaryDTO summary = summaries.get(0);
            String weekStart = summary.getWeekDate().toString();
            String sentAtValue = summary.getSentAt() == null ? null : summary.getSentAt().toString();

            String title = summary.getPlantName();
            String subtitle = "Semana " + weekStart;
            String fileName = "Cascada_" + summary.getPlantName() + "_" + weekStart + ".xlsx";
            String sheetTitle = summary.getPlantName();

            InboxMessageDTO payload = new InboxMessageDTO(
                    summary.getId(),
                    summary.getCascadaId(),
                    title,
                    subtitle,
                    sentAtValue,
                    sentAtValue,
                    fileName,
                    sheetTitle,
                    summary.getCompanyName(),
                    summary.getSentBy(),
                    summary.getPlantId(),
                    weekStart,
                    weekStart,
                    summary.getShiftIds().stream().toList(),
                    summary.getDayKeys().stream().toList(),
                    status
            );

            messagingTemplate.convertAndSend("/topic/inbox/" + recipientUserId, payload);
        }
    }

    private void publishFlexsurInboxMessagesAfterCommit(
            String status,
            Long plantId,
            LocalDate weekDate,
            List<Long> recipientUserIds
    ) {
        Runnable publisher = () -> publishFlexsurInboxMessagesForStatus(status, plantId, weekDate, recipientUserIds);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publisher.run();
                }
            });
            return;
        }
        publisher.run();
    }

    private List<Long> recipientUserIdsForFlexsur(Long plantId, LocalDate weekStartDate, Long shiftId) {
        return cascadaRecipientRepository.findRecipientUserIdsByShift(
                plantId,
                weekStartDate,
                recipientShiftKey(shiftId),
                CascadaType.FLEXSUR
        );
    }

    private String recipientShiftKey(Long shiftId) {
        return shiftId == null ? FLEXSUR_ALL_SHIFTS_RECIPIENT_KEY : shiftId.toString();
    }

    private String stableCascadaId(Long plantId, LocalDate weekStartDate) {
        return "flexsur-" + plantId + "-" + weekStartDate;
    }

    private LocalDate resolveWeekStartDate(FlexsurWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekStartDate();
    }

    private LocalDate resolveWeekEndDate(FlexsurWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekEndDate();
    }

    private Integer resolveWeekNumber(FlexsurWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekNumber();
    }

    private List<FlexsurWeek> findWeeks(Long plantId, LocalDate weekDate, Long shiftId) {
        return shiftId == null
                ? flexsurWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate)
                : flexsurWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftId(plantId, weekDate, shiftId);
    }

    private void deleteWeeks(Long plantId, LocalDate weekDate, Long shiftId) {
        if (shiftId == null) {
            flexsurWeekRepository.deleteByPlantPlantIdAndWeekDate(plantId, weekDate);
            return;
        }
        flexsurWeekRepository.deleteByPlantPlantIdAndWeekDateAndShiftShiftId(plantId, weekDate, shiftId);
    }

    private List<FlexsurManualRow> findManualRows(Long plantId, LocalDate weekDate, Long shiftId) {
        return shiftId == null
                ? flexsurManualRowRepository.findByPlantPlantIdAndWeekDateAndShiftIsNullOrderBySortOrderAscManualFlexsurRowIdAsc(
                        plantId,
                        weekDate
                )
                : flexsurManualRowRepository.findByPlantPlantIdAndWeekDateAndShiftShiftIdOrderBySortOrderAscManualFlexsurRowIdAsc(
                        plantId,
                        weekDate,
                        shiftId
                );
    }

    private Shift resolveShiftIfPresent(Long plantId, Long shiftId) {
        if (shiftId == null) {
            return null;
        }
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        if (shift.getPlant() == null || !plantId.equals(shift.getPlant().getPlantId())) {
            throw new RuntimeException("shiftId does not belong to plant");
        }
        return shift;
    }

    private java.util.Set<String> orderDayKeys(java.util.Set<String> dayKeys) {
        return dayKeys.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(this::dayOrder))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private int compareShiftIds(String left, String right) {
        try {
            return Long.compare(Long.parseLong(left), Long.parseLong(right));
        } catch (NumberFormatException ex) {
            return left.compareTo(right);
        }
    }

    private int dayOrder(String dayKey) {
        return switch (dayKey == null ? "" : dayKey.trim().toLowerCase()) {
            case "lun" -> 1;
            case "mar" -> 2;
            case "mie" -> 3;
            case "jue" -> 4;
            case "vie" -> 5;
            case "sab" -> 6;
            case "dom" -> 7;
            default -> Integer.MAX_VALUE;
        };
    }
}
