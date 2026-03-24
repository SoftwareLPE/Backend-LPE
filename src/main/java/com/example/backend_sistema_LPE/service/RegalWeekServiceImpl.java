package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalDetailDTO;
import com.example.backend_sistema_LPE.dto.CreateRegalManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalManualRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaTripTypeDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.RegalWeeklySummaryDTO;
import com.example.backend_sistema_LPE.dto.UpdateRegalManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.enums.CascadaType;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.RegalDetail;
import com.example.backend_sistema_LPE.model.RegalManualRow;
import com.example.backend_sistema_LPE.model.RegalTripType;
import com.example.backend_sistema_LPE.model.RegalWeek;
import com.example.backend_sistema_LPE.model.RegalWeekSummarySnapshot;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.DriverRouteRepository;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RegalManualRowRepository;
import com.example.backend_sistema_LPE.repository.RegalTripTypeRepository;
import com.example.backend_sistema_LPE.repository.RegalWeekRepository;
import com.example.backend_sistema_LPE.repository.RegalWeekSummarySnapshotRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RegalWeekServiceImpl implements RegalWeekService {
    private static final String REGAL_RECIPIENT_SHIFT_KEY = "REGAL";

    private final RegalWeekRepository regalWeekRepository;
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;
    private final DriverRepository driverRepository;
    private final RegalManualRowRepository regalManualRowRepository;
    private final RegalTripTypeRepository regalTripTypeRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final DriverRouteRepository driverRouteRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final RegalWeekSummarySnapshotRepository regalWeekSummarySnapshotRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public RegalWeekServiceImpl(
            RegalWeekRepository regalWeekRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository,
            DriverRepository driverRepository,
            RegalManualRowRepository regalManualRowRepository,
            RegalTripTypeRepository regalTripTypeRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            DriverRouteRepository driverRouteRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            RegalWeekSummarySnapshotRepository regalWeekSummarySnapshotRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.regalWeekRepository = regalWeekRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
        this.driverRepository = driverRepository;
        this.regalManualRowRepository = regalManualRowRepository;
        this.regalTripTypeRepository = regalTripTypeRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.driverRouteRepository = driverRouteRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
        this.regalWeekSummarySnapshotRepository = regalWeekSummarySnapshotRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public RegalWeekSchemaDTO getRegalWeekSchema(Long plantId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }

        List<RegalWeekSchemaTripTypeDTO> tripTypes = regalTripTypeRepository
                .findByPlantPlantIdAndActiveTrueOrderBySortOrderAsc(plantId)
                .stream()
                .map(this::toSchemaTripTypeDTO)
                .toList();

        return new RegalWeekSchemaDTO(
                plantId,
                List.of("CHOFER", "RUTA", "RECORRIDO"),
                tripTypes,
                "SUM"
        );
    }

    @Override
    public RegalWeekResponseDTO getRegalWeek(Long plantId, LocalDate weekDate, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        List<RegalWeek> weeks = regalWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate);

        List<RegalWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .toList();

        List<RegalManualRow> manualRows = regalManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualRegalRowIdAsc(plantId, weekDate);
        List<RegalTripType> tripTypes = regalTripTypeRepository
                .findByPlantPlantIdAndActiveTrueOrderBySortOrderAsc(plantId);

        List<RegalWeekRowDTO> baseRows = buildBaseRows(plantId, tripTypes);
        baseRows.addAll(buildManualRows(manualRows, tripTypes));
        List<RegalWeekRowDTO> rows = mergeBaseRows(baseRows, savedRows);
        enrichRowTotals(rows, tripTypes);
        RegalWeekTotalsDTO totals = buildTotals(rows, tripTypes);
        applyPersistedWeeklySummary(totals, findSummarySnapshot(plantId, weekDate).orElse(null));

        return new RegalWeekResponseDTO(plantId, weekDate, null, rows, totals);
    }

    @Override
    @Transactional
    public void saveRegalWeek(RegalWeekSaveRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        List<RegalManualRow> manualRows = regalManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualRegalRowIdAsc(
                        request.getPlantId(),
                        request.getWeekDate()
                );

        List<RegalWeek> existingWeeks = regalWeekRepository.findByPlantPlantIdAndWeekDate(
                request.getPlantId(),
                request.getWeekDate()
        );
        if (!existingWeeks.isEmpty()) {
            regalWeekRepository.deleteAll(existingWeeks);
            regalWeekRepository.flush();
        }
        deleteSummarySnapshot(request.getPlantId(), request.getWeekDate());

        List<RegalWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        for (RegalWeekRowDTO row : rows) {
            RegalWeek week = new RegalWeek();
            week.setPlant(plant);
            week.setShift(null);
            week.setWeekDate(request.getWeekDate());
            week.setStatus(CascadaStatus.DRAFT);

            RegalManualRow manualRow = resolveManualRow(row, manualRows);
            if (isManualRow(row) && manualRow == null) {
                throw new RuntimeException("Manual row not found: " + row.getManualRegalRowId());
            }
            if (manualRow != null) {
                syncManualRowFromWeekRow(manualRow, row, userId);
            }

            if (manualRow != null) {
                week.setManualRow(manualRow);
            } else if (row.getDriverId() != null) {
                Driver driver = driverRepository.findById(row.getDriverId())
                        .orElseThrow(() -> new RuntimeException("Driver not found: " + row.getDriverId()));
                week.setDriver(driver);
            }

            LocalDateTime now = LocalDateTime.now();
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(userId);

            List<RegalDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            List<RegalDetail> detailEntities = new ArrayList<>();
            for (RegalDetailDTO detailDTO : details) {
                RegalTripType tripType = resolveTripType(request.getPlantId(), detailDTO);
                if (tripType == null) {
                    throw new RuntimeException("tripTypeId or tripTypeCode is required");
                }
                if (detailDTO.getDayOfWeek() == null || detailDTO.getDayOfWeek().isBlank()) {
                    throw new RuntimeException("dayOfWeek is required");
                }
                RegalDetail detail = new RegalDetail();
                detail.setRegalWeek(week);
                detail.setTripType(tripType);
                detail.setDayOfWeek(detailDTO.getDayOfWeek().trim());
                detail.setTripCount(detailDTO.getTripCount() == null ? 0 : detailDTO.getTripCount());
                detailEntities.add(detail);
            }
            week.setDetails(detailEntities);
            regalWeekRepository.save(week);
        }

        saveSummarySnapshot(plant, request.getWeekDate(), request.getTotals(), userId);
    }

    @Override
    @Transactional
    public RegalManualRowDTO createManualRow(CreateRegalManualRowRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        String driverNameOverride = trimToNull(request.getDriverNameOverride());
        if (driverNameOverride == null) {
            throw new RuntimeException("driverNameOverride is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        List<RegalManualRow> existing = regalManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualRegalRowIdAsc(
                        request.getPlantId(),
                        request.getWeekDate()
                );

        int nextSortOrder = existing.stream()
                .map(RegalManualRow::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(existing.size());

        RegalManualRow manualRow = new RegalManualRow();
        manualRow.setPlant(plant);
        manualRow.setWeekDate(request.getWeekDate());
        manualRow.setDriverNameOverride(driverNameOverride);
        manualRow.setRouteName(trimToNull(request.getRouteName()));
        manualRow.setRouteLocation(trimToNull(request.getRouteLocation()));
        manualRow.setSortOrder(request.getSortOrder() == null ? nextSortOrder : request.getSortOrder());
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        return toManualRowDTO(regalManualRowRepository.save(manualRow));
    }

    @Override
    @Transactional
    public RegalManualRowDTO updateManualRow(
            Long manualRegalRowId,
            UpdateRegalManualRowRequestDTO request,
            Long userId
    ) {
        if (manualRegalRowId == null) {
            throw new RuntimeException("manualRegalRowId is required");
        }

        RegalManualRow manualRow = regalManualRowRepository.findById(manualRegalRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));

        if (request.getDriverNameOverride() != null) {
            String driverNameOverride = trimToNull(request.getDriverNameOverride());
            if (driverNameOverride == null) {
                throw new RuntimeException("driverNameOverride is required");
            }
            manualRow.setDriverNameOverride(driverNameOverride);
        }
        if (request.getRouteName() != null) {
            manualRow.setRouteName(trimToNull(request.getRouteName()));
        }
        if (request.getRouteLocation() != null) {
            manualRow.setRouteLocation(trimToNull(request.getRouteLocation()));
        }
        if (request.getSortOrder() != null) {
            manualRow.setSortOrder(request.getSortOrder());
        }
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        return toManualRowDTO(regalManualRowRepository.save(manualRow));
    }

    @Override
    @Transactional
    public void deleteManualRow(Long manualRegalRowId) {
        if (manualRegalRowId == null) {
            throw new RuntimeException("manualRegalRowId is required");
        }

        RegalManualRow manualRow = regalManualRowRepository.findById(manualRegalRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));
        deleteManualRowsAndCounts(List.of(manualRow));
    }

    @Override
    @Transactional
    public void updateRegalStatus(Long plantId, LocalDate weekDate, String status, Long userId) {
        updateRegalStatus(plantId, weekDate, status, userId, null);
    }

    @Override
    @Transactional
    public void updateRegalStatus(
            Long plantId,
            LocalDate weekDate,
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

        List<RegalWeek> weeks = regalWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate);
        if (weeks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (targetStatus == CascadaStatus.SENT && (recipientUserIds == null || recipientUserIds.stream().filter(java.util.Objects::nonNull).toList().isEmpty())) {
            throw new RuntimeException("recipientUserIds is required");
        }
        for (RegalWeek week : weeks) {
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
        regalWeekRepository.saveAll(weeks);

        LocalDate effectiveWeekStartDate = resolveWeekStartDate(weeks.get(0));
        if (targetStatus == CascadaStatus.SENT) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));
            cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndCascadaType(
                    plantId,
                    effectiveWeekStartDate,
                    REGAL_RECIPIENT_SHIFT_KEY,
                    CascadaType.REGAL
            );
            List<CascadaRecipient> recipients = new ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                if (recipientUserId == null) {
                    continue;
                }
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(effectiveWeekStartDate);
                recipient.setShiftId(REGAL_RECIPIENT_SHIFT_KEY);
                recipient.setDayKey(null);
                recipient.setCascadaType(CascadaType.REGAL);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);
            publishRegalInboxMessagesAfterCommit(
                    targetStatus.name(),
                    plantId,
                    effectiveWeekStartDate,
                    recipientUserIds
            );
            return;
        }

        if (targetStatus == CascadaStatus.DELETED) {
            publishRegalInboxMessagesAfterCommit(
                    targetStatus.name(),
                    plantId,
                    effectiveWeekStartDate,
                    recipientUserIdsForRegal(plantId, effectiveWeekStartDate)
            );
        }
    }

    @Override
    public List<CascadaSummaryDTO> getRegalSummaries(String status, Long plantId, LocalDate weekDate, Long recipientUserId) {
        String effectiveStatus = (status == null || status.isBlank()) ? CascadaStatus.SENT.name() : status;
        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(effectiveStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + effectiveStatus);
        }

        List<RegalWeek> weeks = regalWeekRepository.findByStatus(cascadaStatus);
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
                    .findByRecipientUserIdAndCascadaType(recipientUserId, CascadaType.REGAL);
            java.util.Set<String> allowedKeys = recipients.stream()
                    .map(r -> r.getPlant().getPlantId() + "|" + r.getWeekStartDate() + "|" + r.getShiftId())
                    .collect(java.util.stream.Collectors.toSet());

            weeks = weeks.stream()
                    .filter(week -> allowedKeys.contains(
                            week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week) + "|" + REGAL_RECIPIENT_SHIFT_KEY
                    ))
                    .toList();
        }

        java.util.Map<String, List<RegalWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week)
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<RegalWeek> groupWeeks = entry.getValue();
            RegalWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(RegalWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            LocalDate effectiveWeekStartDate = resolveWeekStartDate(latest);
            java.util.Set<String> shiftIds = groupWeeks.stream()
                    .map(week -> week.getShift() == null ? null : week.getShift().getShiftId().toString())
                    .filter(java.util.Objects::nonNull)
                    .sorted(this::compareShiftIds)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            java.util.Set<String> dayKeys = groupWeeks.stream()
                    .flatMap(week -> week.getDetails().stream().map(RegalDetail::getDayOfWeek))
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getRegalWeekId(),
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
                    orderedDayKeySet(dayKeys),
                    latest.getSentAt()
            ));
        }

        summaries.sort(java.util.Comparator.comparing(
                CascadaSummaryDTO::getSentAt,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ).reversed());

        return summaries;
    }

    private RegalWeekRowDTO toRowDTO(RegalWeek week) {
        List<RegalDetailDTO> details = week.getDetails() == null
                ? List.of()
                : week.getDetails().stream()
                        .map(detail -> new RegalDetailDTO(
                                detail.getTripType() == null ? null : detail.getTripType().getTripTypeId(),
                                detail.getTripType() == null ? null : detail.getTripType().getCode(),
                                detail.getDayOfWeek(),
                                detail.getTripCount()
                        ))
                        .toList();

        Long driverId = week.getDriver() == null ? null : week.getDriver().getDriverId();
        DriverPlantAssignment assignment = driverId == null
                ? null
                : driverPlantAssignmentRepository.findByDriverDriverIdAndPlantPlantId(
                        driverId,
                        week.getPlant().getPlantId()
                ).orElse(null);

        return new RegalWeekRowDTO(
                week.getRegalWeekId(),
                week.getManualRow() == null ? null : week.getManualRow().getManualRegalRowId(),
                driverId,
                week.getManualRow() == null ? week.getDriver() == null ? null : week.getDriver().getDriverName() : week.getManualRow().getDriverNameOverride(),
                week.getManualRow() == null ? week.getDriver() == null ? null : week.getDriver().getLastName() : null,
                resolveDriverType(driverId, week.getPlant().getPlantId(), assignment),
                week.getManualRow() == null ? assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteId() : null,
                week.getManualRow() == null ? assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteName() : week.getManualRow().getRouteName(),
                week.getManualRow() == null ? assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getLocation() : week.getManualRow().getRouteLocation(),
                details,
                null,
                null
        );
    }

    private List<RegalWeekRowDTO> buildBaseRows(Long plantId, List<RegalTripType> tripTypes) {
        List<Driver> drivers = driverRepository.findByPlantPlantIdAndActiveTrue(plantId);
        List<RegalWeekRowDTO> rows = new ArrayList<>();

        for (Driver driver : drivers) {
            DriverPlantAssignment assignment = driverPlantAssignmentRepository
                    .findByDriverDriverIdAndPlantPlantId(driver.getDriverId(), plantId)
                    .orElse(null);

            List<RegalDetailDTO> details = buildEmptyDetails(tripTypes);

            rows.add(new RegalWeekRowDTO(
                    null,
                    null,
                    driver.getDriverId(),
                    driver.getDriverName(),
                    driver.getLastName(),
                    resolveDriverType(driver.getDriverId(), plantId, assignment),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteId(),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteName(),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getLocation(),
                    details,
                    null,
                    null
            ));
        }

        return rows;
    }

    private List<RegalWeekRowDTO> buildManualRows(
            List<RegalManualRow> manualRows,
            List<RegalTripType> tripTypes
    ) {
        if (manualRows == null || manualRows.isEmpty()) {
            return List.of();
        }
        return manualRows.stream()
                .map(row -> new RegalWeekRowDTO(
                        null,
                        row.getManualRegalRowId(),
                        null,
                        row.getDriverNameOverride(),
                        null,
                        null,
                        null,
                        row.getRouteName(),
                        row.getRouteLocation(),
                        buildEmptyDetails(tripTypes),
                        null,
                        null
                ))
                .toList();
    }

    private List<RegalDetailDTO> buildEmptyDetails(List<RegalTripType> tripTypes) {
        List<RegalDetailDTO> details = new ArrayList<>();
        for (RegalTripType tripType : tripTypes) {
            if (tripType.getDayKeys() == null || tripType.getDayKeys().isEmpty()) {
                continue;
            }
            for (String dayKey : tripType.getDayKeys()) {
                details.add(new RegalDetailDTO(
                        tripType.getTripTypeId(),
                        tripType.getCode(),
                        dayKey,
                        0
                ));
            }
        }
        return details;
    }

    private List<RegalWeekRowDTO> mergeBaseRows(
            List<RegalWeekRowDTO> baseRows,
            List<RegalWeekRowDTO> savedRows
    ) {
        java.util.Map<String, RegalWeekRowDTO> baseByKey = baseRows.stream()
                .map(row -> java.util.Map.entry(rowKey(row), row))
                .filter(entry -> entry.getKey() != null)
                .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue, (a, b) -> a));

        List<RegalWeekRowDTO> merged = new ArrayList<>(baseRows);

        for (RegalWeekRowDTO saved : savedRows) {
            RegalWeekRowDTO base = baseByKey.get(rowKey(saved));
            if (base == null) {
                merged.add(saved);
                continue;
            }
            mergeDetails(base, saved);
        }

        return merged;
    }

    private void mergeDetails(RegalWeekRowDTO base, RegalWeekRowDTO saved) {
        if (base.getRegalWeekId() == null && saved.getRegalWeekId() != null) {
            base.setRegalWeekId(saved.getRegalWeekId());
        }
        if (base.getManualRegalRowId() == null && saved.getManualRegalRowId() != null) {
            base.setManualRegalRowId(saved.getManualRegalRowId());
        }
        if (base.getDriverId() == null && saved.getDriverId() != null) {
            base.setDriverId(saved.getDriverId());
        }
        if (base.getDriverName() == null && saved.getDriverName() != null) {
            base.setDriverName(saved.getDriverName());
        }
        if (base.getDriverLastName() == null && saved.getDriverLastName() != null) {
            base.setDriverLastName(saved.getDriverLastName());
        }
        if (base.getDriverType() == null && saved.getDriverType() != null) {
            base.setDriverType(saved.getDriverType());
        }
        if (base.getRouteId() == null && saved.getRouteId() != null) {
            base.setRouteId(saved.getRouteId());
        }
        if (base.getRouteName() == null && saved.getRouteName() != null) {
            base.setRouteName(saved.getRouteName());
        }
        if (base.getRecorrido() == null && saved.getRecorrido() != null) {
            base.setRecorrido(saved.getRecorrido());
        }

        java.util.Map<String, RegalDetailDTO> baseByKey = (base.getDetails() == null ? List.<RegalDetailDTO>of() : base.getDetails())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        detail -> detail.getTripTypeId() + "|" + detail.getDayOfWeek(),
                        detail -> detail,
                        (a, b) -> a
                ));

        List<RegalDetailDTO> savedDetails = saved.getDetails() == null ? List.of() : saved.getDetails();
        for (RegalDetailDTO savedDetail : savedDetails) {
            String key = savedDetail.getTripTypeId() + "|" + savedDetail.getDayOfWeek();
            RegalDetailDTO baseDetail = baseByKey.get(key);
            if (baseDetail == null) {
                if (base.getDetails() == null) {
                    base.setDetails(new ArrayList<>());
                }
                base.getDetails().add(savedDetail);
                continue;
            }
            baseDetail.setTripCount(savedDetail.getTripCount());
        }
    }

    private String rowKey(RegalWeekRowDTO row) {
        if (row.getManualRegalRowId() != null) {
            return "M:" + row.getManualRegalRowId();
        }
        return row.getDriverId() == null ? null : "D:" + row.getDriverId();
    }

    private RegalTripType resolveTripType(Long plantId, RegalDetailDTO detailDTO) {
        if (detailDTO.getTripTypeId() != null) {
            return regalTripTypeRepository.findByTripTypeIdAndPlantPlantId(
                    detailDTO.getTripTypeId(),
                    plantId
            ).orElse(null);
        }
        if (detailDTO.getTripTypeCode() != null && !detailDTO.getTripTypeCode().isBlank()) {
            return regalTripTypeRepository.findByPlantPlantIdAndCodeIgnoreCase(
                    plantId,
                    detailDTO.getTripTypeCode().trim()
            ).orElse(null);
        }
        return null;
    }

    private String resolveDriverType(Long driverId, Long plantId, DriverPlantAssignment assignment) {
        if (assignment != null && assignment.getDriverType() != null) {
            return assignment.getDriverType().name();
        }
        if (driverId == null) {
            return null;
        }
        return driverRouteRepository.findByDriverDriverId(driverId).stream()
                .filter(driverRoute -> driverRoute.getDriverType() != null)
                .filter(driverRoute -> driverRoute.getRoute() == null
                        || driverRoute.getRoute().getPlant() == null
                        || plantId.equals(driverRoute.getRoute().getPlant().getPlantId()))
                .map(driverRoute -> driverRoute.getDriverType().name())
                .findFirst()
                .orElse(null);
    }

    private RegalManualRow resolveManualRow(RegalWeekRowDTO row, List<RegalManualRow> manualRows) {
        if (row == null || row.getManualRegalRowId() == null) {
            return null;
        }
        if (manualRows != null && !manualRows.isEmpty()) {
            RegalManualRow persisted = manualRows.stream()
                    .filter(manualRow -> row.getManualRegalRowId().equals(manualRow.getManualRegalRowId()))
                    .findFirst()
                    .orElse(null);
            if (persisted != null) {
                return persisted;
            }
        }
        return regalManualRowRepository.findById(row.getManualRegalRowId()).orElse(null);
    }

    private boolean isManualRow(RegalWeekRowDTO row) {
        if (row == null) {
            return false;
        }
        if (row.getManualRegalRowId() != null) {
            return true;
        }
        return row.getDriverId() == null && trimToNull(row.getDriverName()) != null;
    }

    private RegalManualRowDTO toManualRowDTO(RegalManualRow row) {
        return new RegalManualRowDTO(
                row.getManualRegalRowId(),
                row.getDriverNameOverride(),
                row.getRouteName(),
                row.getRouteLocation(),
                row.getSortOrder()
        );
    }

    private void syncManualRowFromWeekRow(RegalManualRow manualRow, RegalWeekRowDTO row, Long userId) {
        if (manualRow == null || row == null) {
            return;
        }
        String driverName = trimToNull(row.getDriverName());
        if (driverName != null) {
            manualRow.setDriverNameOverride(driverName);
        }
        if (row.getRouteName() != null) {
            manualRow.setRouteName(trimToNull(row.getRouteName()));
        }
        if (row.getRecorrido() != null) {
            manualRow.setRouteLocation(trimToNull(row.getRecorrido()));
        }
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);
        regalManualRowRepository.save(manualRow);
    }

    private RegalWeekSchemaTripTypeDTO toSchemaTripTypeDTO(RegalTripType tripType) {
        return new RegalWeekSchemaTripTypeDTO(
                tripType.getTripTypeId(),
                tripType.getCode(),
                tripType.getLabel(),
                tripType.getSortOrder(),
                orderDayKeys(tripType.getDayKeys()),
                "TOTAL"
        );
    }

    private RegalWeekTotalsDTO buildTotals(List<RegalWeekRowDTO> rows, List<RegalTripType> tripTypes) {
        java.util.Map<Long, java.util.Map<String, Integer>> byTripTypeDay = new LinkedHashMap<>();
        java.util.Map<Long, Integer> byTripType = new LinkedHashMap<>();

        for (RegalTripType tripType : tripTypes) {
            java.util.Map<String, Integer> dayTotals = new LinkedHashMap<>();
            for (String dayKey : orderDayKeys(tripType.getDayKeys())) {
                dayTotals.put(dayKey, 0);
            }
            byTripTypeDay.put(tripType.getTripTypeId(), dayTotals);
            byTripType.put(tripType.getTripTypeId(), 0);
        }

        int weekTotal = 0;
        for (RegalWeekRowDTO row : rows) {
            List<RegalDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            for (RegalDetailDTO detail : details) {
                if (detail.getTripTypeId() == null || detail.getDayOfWeek() == null) {
                    continue;
                }
                int count = detail.getTripCount() == null ? 0 : detail.getTripCount();
                java.util.Map<String, Integer> dayTotals = byTripTypeDay.computeIfAbsent(
                        detail.getTripTypeId(),
                        key -> new LinkedHashMap<>()
                );
                dayTotals.put(detail.getDayOfWeek(), dayTotals.getOrDefault(detail.getDayOfWeek(), 0) + count);
                byTripType.put(detail.getTripTypeId(), byTripType.getOrDefault(detail.getTripTypeId(), 0) + count);
                weekTotal += count;
            }
        }

        return new RegalWeekTotalsDTO(byTripTypeDay, byTripType, weekTotal, null);
    }

    private Optional<RegalWeekSummarySnapshot> findSummarySnapshot(Long plantId, LocalDate weekDate) {
        return regalWeekSummarySnapshotRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate);
    }

    private void deleteSummarySnapshot(Long plantId, LocalDate weekDate) {
        findSummarySnapshot(plantId, weekDate).ifPresent(regalWeekSummarySnapshotRepository::delete);
    }

    private void saveSummarySnapshot(
            Plant plant,
            LocalDate weekDate,
            RegalWeekTotalsDTO totals,
            Long userId
    ) {
        RegalWeeklySummaryDTO weeklySummary = totals == null ? null : totals.getWeeklySummary();
        if (weeklySummary == null) {
            return;
        }

        RegalWeekSummarySnapshot snapshot = new RegalWeekSummarySnapshot();
        snapshot.setPlant(plant);
        snapshot.setWeekDate(weekDate);
        snapshot.setNormalShort(zeroIfNull(weeklySummary.getNormalShort()));
        snapshot.setNormalLong(zeroIfNull(weeklySummary.getNormalLong()));
        snapshot.setExtraShort(zeroIfNull(weeklySummary.getExtraShort()));
        snapshot.setExtraLong(zeroIfNull(weeklySummary.getExtraLong()));
        snapshot.setUpdatedAt(LocalDateTime.now());
        snapshot.setUpdatedByUserId(userId);
        regalWeekSummarySnapshotRepository.save(snapshot);
    }

    private void applyPersistedWeeklySummary(RegalWeekTotalsDTO totals, RegalWeekSummarySnapshot snapshot) {
        if (totals == null || snapshot == null) {
            return;
        }
        totals.setWeeklySummary(new RegalWeeklySummaryDTO(
                snapshot.getNormalShort(),
                snapshot.getNormalLong(),
                snapshot.getExtraShort(),
                snapshot.getExtraLong()
        ));
    }

    private int zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private void enrichRowTotals(List<RegalWeekRowDTO> rows, List<RegalTripType> tripTypes) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> orderedTripTypeIds = tripTypes == null
                ? List.of()
                : tripTypes.stream()
                        .map(RegalTripType::getTripTypeId)
                        .toList();

        for (RegalWeekRowDTO row : rows) {
            java.util.Map<Long, Integer> totalsByTripType = new LinkedHashMap<>();
            for (Long tripTypeId : orderedTripTypeIds) {
                totalsByTripType.put(tripTypeId, 0);
            }

            int rowTotal = 0;
            List<RegalDetailDTO> details = row.getDetails() == null ? List.of() : row.getDetails();
            for (RegalDetailDTO detail : details) {
                if (detail.getTripTypeId() == null) {
                    continue;
                }
                int count = detail.getTripCount() == null ? 0 : detail.getTripCount();
                totalsByTripType.put(
                        detail.getTripTypeId(),
                        totalsByTripType.getOrDefault(detail.getTripTypeId(), 0) + count
                );
                rowTotal += count;
            }

            row.setTotalsByTripType(totalsByTripType);
            row.setRowTotal(rowTotal);
        }
    }

    private List<String> orderDayKeys(Set<String> dayKeys) {
        if (dayKeys == null || dayKeys.isEmpty()) {
            return List.of();
        }
        List<String> ordered = List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom");
        return ordered.stream().filter(dayKeys::contains).toList();
    }

    private void deleteManualRowsAndCounts(List<RegalManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return;
        }
        List<Long> ids = manualRows.stream()
                .map(RegalManualRow::getManualRegalRowId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!ids.isEmpty()) {
            List<RegalWeek> weeks = regalWeekRepository.findByManualRowManualRegalRowIdIn(ids);
            if (!weeks.isEmpty()) {
                regalWeekRepository.deleteAll(weeks);
            }
        }
        regalManualRowRepository.deleteAll(manualRows);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private void publishRegalInboxMessagesForStatus(
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
            List<CascadaSummaryDTO> summaries = getRegalSummaries(status, plantId, weekDate, recipientUserId);
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

    private void publishRegalInboxMessagesAfterCommit(
            String status,
            Long plantId,
            LocalDate weekDate,
            List<Long> recipientUserIds
    ) {
        Runnable publisher = () -> publishRegalInboxMessagesForStatus(status, plantId, weekDate, recipientUserIds);
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

    private List<Long> recipientUserIdsForRegal(Long plantId, LocalDate weekStartDate) {
        return cascadaRecipientRepository.findRecipientUserIdsByShift(
                plantId,
                weekStartDate,
                REGAL_RECIPIENT_SHIFT_KEY,
                CascadaType.REGAL
        );
    }

    private String stableCascadaId(Long plantId, LocalDate weekStartDate) {
        return "regal-" + plantId + "-" + weekStartDate;
    }

    private LocalDate resolveWeekStartDate(RegalWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekStartDate();
    }

    private LocalDate resolveWeekEndDate(RegalWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekEndDate();
    }

    private Integer resolveWeekNumber(RegalWeek week) {
        return WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekNumber();
    }

    private Set<String> orderedDayKeySet(Set<String> dayKeys) {
        return dayKeys.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(this::dayOrder))
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
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

    private int compareShiftIds(String left, String right) {
        try {
            return Long.compare(Long.parseLong(left), Long.parseLong(right));
        } catch (NumberFormatException ex) {
            return String.valueOf(left).compareTo(String.valueOf(right));
        }
    }
}
