package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateFormatWeekManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekCellDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekManualRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTurnDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.dto.UpdateFormatWeekManualRowRequestDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.FormatWeek;
import com.example.backend_sistema_LPE.model.FormatWeekCell;
import com.example.backend_sistema_LPE.model.FormatWeekManualRow;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.enums.CascadaType;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.FormatWeekRepository;
import com.example.backend_sistema_LPE.repository.FormatWeekManualRowRepository;
import com.example.backend_sistema_LPE.repository.ShiftFormatTurnMapRepository;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormatWeekServiceImpl implements FormatWeekService {
    private final FormatWeekRepository formatWeekRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final FormatTurnConfigRepository formatTurnConfigRepository;
    private final FormatWeekManualRowRepository formatWeekManualRowRepository;
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;
    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final ShiftFormatTurnMapRepository shiftFormatTurnMapRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FormatWeekServiceImpl(
            FormatWeekRepository formatWeekRepository,
            FormatTypeRepository formatTypeRepository,
            FormatTurnConfigRepository formatTurnConfigRepository,
            FormatWeekManualRowRepository formatWeekManualRowRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository,
            RouteRepository routeRepository,
            DriverRepository driverRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            ShiftFormatTurnMapRepository shiftFormatTurnMapRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.formatWeekRepository = formatWeekRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.formatTurnConfigRepository = formatTurnConfigRepository;
        this.formatWeekManualRowRepository = formatWeekManualRowRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.shiftFormatTurnMapRepository = shiftFormatTurnMapRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public FormatWeekResponseDTO getFormatWeek(Long plantId, Long formatTypeId, LocalDate weekDate, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        List<FormatWeek> weeks = findFormatWeeksByRequestedWeek(plantId, formatTypeId, weekDate, shiftId);

        List<FormatTurnConfig> allTurnConfigs = formatTurnConfigRepository
                .findByFormatTypeFormatTypeId(formatTypeId);
        Map<Long, Long> shiftIdByTurnConfigId = buildShiftIdByTurnConfigId(plantId, formatTypeId, allTurnConfigs);
        List<FormatTurnConfig> turnConfigs = filterTurnConfigsForShift(allTurnConfigs, shiftIdByTurnConfigId, shiftId);

        Map<Long, FormatTurnConfig> turnConfigById = allTurnConfigs.stream()
                .collect(Collectors.toMap(FormatTurnConfig::getTurnConfigId, config -> config));
        WeekMetadataResolver.ResolvedWeekMetadata responseWeekMetadata = weeks.isEmpty()
                ? WeekMetadataResolver.resolve(weekDate, null, null, null)
                : WeekMetadataResolver.resolve(
                        weekDate,
                        weeks.get(0).getWeekStartDate(),
                        weeks.get(0).getWeekEndDate(),
                        weeks.get(0).getWeekNumber()
                );

        List<FormatWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .map(row -> filterRowCellsByShift(row, shiftIdByTurnConfigId, turnConfigById, shiftId))
                .toList();

        if (shiftId == null) {
            List<FormatWeekRowDTO> rows = new ArrayList<>(savedRows);
            enrichRowTotals(rows);
            return new FormatWeekResponseDTO(
                    plantId,
                    weekDate,
                    responseWeekMetadata.getWeekStartDate(),
                    responseWeekMetadata.getWeekEndDate(),
                    responseWeekMetadata.getWeekNumber(),
                    shiftId,
                    formatTypeId,
                    rows,
                    buildTotals(rows, turnConfigs)
            );
        }

        FormatType formatType = formatTypeRepository.findById(formatTypeId)
                .orElseThrow(() -> new RuntimeException("Format type not found"));
        boolean usesDriver = usesDriver(formatType);
        Map<String, FormatWeekRowDTO> weeklyOverrideRows = resolveWeeklyOverrideRows(
                findFormatWeeksByRequestedWeek(plantId, formatTypeId, weekDate, null),
                usesDriver
        );
        List<FormatWeekManualRow> manualRows = formatWeekManualRowRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateOrderBySortOrderAscManualRowIdAsc(
                        plantId,
                        formatTypeId,
                        weekDate
                );

        savedRows.forEach(row -> applyWeeklyBaseOverrides(row, weeklyOverrideRows.get(rowKey(row, usesDriver))));
        List<FormatWeekRowDTO> rows = new ArrayList<>(savedRows);

        if (!turnConfigs.isEmpty()) {
            List<FormatWeekRowDTO> baseRows = usesDriver
                    ? buildDriverRows(plantId, shiftId, formatType, turnConfigs)
                    : buildRouteRows(plantId, formatType, turnConfigs);
            baseRows = new ArrayList<>(baseRows);
            baseRows.addAll(buildManualRows(manualRows, turnConfigs));
            baseRows.forEach(row -> applyWeeklyBaseOverrides(row, weeklyOverrideRows.get(rowKey(row, usesDriver))));

            if (!baseRows.isEmpty()) {
                rows = mergeBaseRows(baseRows, savedRows, usesDriver);
            }
        }

        enrichRowTotals(rows);
        return new FormatWeekResponseDTO(
                plantId,
                weekDate,
                responseWeekMetadata.getWeekStartDate(),
                responseWeekMetadata.getWeekEndDate(),
                responseWeekMetadata.getWeekNumber(),
                shiftId,
                formatTypeId,
                rows,
                buildTotals(rows, turnConfigs)
        );
    }

    @Override
    @Transactional
    public void saveFormatWeek(FormatWeekSaveRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        if (request.getShiftId() == null) {
            throw new RuntimeException("shiftId is required");
        }
        if (request.getFormatTypeId() == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                request.getWeekDate(),
                request.getWeekStartDate(),
                request.getWeekEndDate(),
                request.getWeekNumber()
        );
        LocalDate requestedWeekDate = request.getWeekDate();
        request.setWeekDate(weekMetadata.getWeekStartDate());
        request.setWeekStartDate(weekMetadata.getWeekStartDate());
        request.setWeekEndDate(weekMetadata.getWeekEndDate());
        request.setWeekNumber(weekMetadata.getWeekNumber());

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        FormatType formatType = formatTypeRepository.findById(request.getFormatTypeId())
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        List<FormatTurnConfig> allTurnConfigs = formatTurnConfigRepository
                .findByFormatTypeFormatTypeId(request.getFormatTypeId())
                ;
        Map<Long, FormatTurnConfig> turnConfigById = allTurnConfigs.stream()
                .collect(Collectors.toMap(FormatTurnConfig::getTurnConfigId, c -> c));
        Map<Long, Long> shiftIdByTurnConfigId = buildShiftIdByTurnConfigId(
                request.getPlantId(),
                request.getFormatTypeId(),
                allTurnConfigs
        );

        List<FormatWeekManualRow> persistedManualRows = saveManualRows(
                request,
                plant,
                formatType,
                userId
        );
        List<FormatWeek> existingWeeks = findFormatWeeksByRequestedWeek(
                request.getPlantId(),
                request.getFormatTypeId(),
                requestedWeekDate,
                request.getShiftId()
        );
        if (!existingWeeks.isEmpty()) {
            formatWeekRepository.deleteAll(existingWeeks);
            formatWeekRepository.flush();
        }

        List<FormatWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        for (FormatWeekRowDTO row : rows) {
            FormatWeek week = new FormatWeek();
            week.setPlant(plant);
            week.setShift(shift);
            week.setFormatType(formatType);
            week.setWeekDate(request.getWeekDate());
            week.setWeekStartDate(request.getWeekStartDate());
            week.setWeekEndDate(request.getWeekEndDate());
            week.setWeekNumber(request.getWeekNumber());
            week.setStatus(CascadaStatus.DRAFT);

            FormatWeekManualRow manualRow = resolveManualRow(row, persistedManualRows);
            if (isManualRow(row) && manualRow == null) {
                throw new RuntimeException("Manual row not found: " + row.getManualRowId());
            }
            if (manualRow != null) {
                week.setManualRow(manualRow);
                week.setRouteNameOverride(null);
                week.setDriverNameOverride(null);
                week.setDriverLastNameOverride(null);
                week.setUnitType(manualRow.getUnitType());
                week.setSecondaryValue(manualRow.getSecondaryValue());
            } else {
                week.setRouteNameOverride(trimToNull(row.getRouteName()));
                week.setDriverNameOverride(trimToNull(row.getDriverName()));
                week.setDriverLastNameOverride(trimToNull(row.getDriverLastName()));
                week.setUnitType(row.getUnitType());
                week.setSecondaryValue(row.getSecondaryValue());
            }

            if (manualRow == null && row.getRouteId() != null) {
                Route route = routeRepository.findById(row.getRouteId())
                        .orElseThrow(() -> new RuntimeException("Route not found: " + row.getRouteId()));
                week.setRoute(route);
            }
            if (manualRow == null && row.getDriverId() != null) {
                Driver driver = driverRepository.findById(row.getDriverId())
                        .orElseThrow(() -> new RuntimeException("Driver not found: " + row.getDriverId()));
                week.setDriver(driver);
            }

            LocalDateTime now = LocalDateTime.now();
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(userId);

            List<FormatWeekCellDTO> cells = row.getCells() == null ? List.of() : row.getCells();
            List<FormatWeekCell> weekCells = new ArrayList<>();
            for (FormatWeekCellDTO cellDTO : cells) {
                if (cellDTO.getTurnConfigId() == null) {
                    throw new RuntimeException("turnConfigId is required");
                }
                FormatTurnConfig config = turnConfigById.get(cellDTO.getTurnConfigId());
                if (config == null) {
                    throw new RuntimeException("Invalid turnConfigId: " + cellDTO.getTurnConfigId());
                }
                Long mappedShiftId = shiftIdByTurnConfigId.get(cellDTO.getTurnConfigId());
                if (mappedShiftId == null && !isFormatLevelColumn(config.getTurnName())) {
                    throw new RuntimeException("turnConfigId has no shift mapping: " + cellDTO.getTurnConfigId());
                }
                if (mappedShiftId != null && !mappedShiftId.equals(request.getShiftId())) {
                    throw new RuntimeException("turnConfigId does not belong to shiftId: " + cellDTO.getTurnConfigId());
                }
                if (cellDTO.getDayOfWeek() != null
                        && config.getDayOfWeek() != null
                        && !config.getDayOfWeek().equalsIgnoreCase(cellDTO.getDayOfWeek().trim())) {
                    throw new RuntimeException("turnConfigId/dayOfWeek mismatch: " + cellDTO.getTurnConfigId());
                }
                FormatWeekCell cell = new FormatWeekCell();
                cell.setFormatWeek(week);
                cell.setTurnConfig(config);
                cell.setDayOfWeek(cellDTO.getDayOfWeek());
                cell.setTripCount(cellDTO.getTripCount() == null ? 0 : cellDTO.getTripCount());
                weekCells.add(cell);
            }
            week.setCells(weekCells);
            formatWeekRepository.save(week);
        }
    }

    @Override
    @Transactional
    public FormatWeekManualRowDTO createManualRow(CreateFormatWeekManualRowRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getFormatTypeId() == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        FormatType formatType = formatTypeRepository.findById(request.getFormatTypeId())
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        if (plant.getFormatTypeId() == null || !request.getFormatTypeId().equals(plant.getFormatTypeId())) {
            throw new RuntimeException("formatTypeId does not belong to plant");
        }

        List<FormatWeekManualRow> existing = formatWeekManualRowRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateOrderBySortOrderAscManualRowIdAsc(
                        request.getPlantId(),
                        request.getFormatTypeId(),
                        request.getWeekDate()
                );

        int nextSortOrder = existing.stream()
                .map(FormatWeekManualRow::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(existing.size());

        FormatWeekManualRow manualRow = new FormatWeekManualRow();
        manualRow.setPlant(plant);
        manualRow.setFormatType(formatType);
        manualRow.setWeekDate(request.getWeekDate());
        manualRow.setRouteName(trimToNull(request.getRouteName()));
        manualRow.setDriverName(trimToNull(request.getDriverName()));
        manualRow.setDriverLastName(trimToNull(request.getDriverLastName()));
        manualRow.setUnitType(trimToNull(request.getUnitType()));
        manualRow.setSecondaryValue(trimToNull(request.getSecondaryValue()));
        manualRow.setExtraRow(request.getExtraRow() == null ? Boolean.TRUE : request.getExtraRow());
        manualRow.setSortOrder(request.getSortOrder() == null ? nextSortOrder : request.getSortOrder());
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        FormatWeekManualRow saved = formatWeekManualRowRepository.save(manualRow);
        return new FormatWeekManualRowDTO(
                saved.getManualRowId(),
                saved.getRouteName(),
                saved.getDriverName(),
                saved.getDriverLastName(),
                saved.getUnitType(),
                saved.getSecondaryValue(),
                saved.getExtraRow(),
                saved.getSortOrder()
        );
    }

    @Override
    @Transactional
    public FormatWeekManualRowDTO updateManualRow(
            Long manualRowId,
            UpdateFormatWeekManualRowRequestDTO request,
            Long userId
    ) {
        if (manualRowId == null) {
            throw new RuntimeException("manualRowId is required");
        }

        FormatWeekManualRow manualRow = formatWeekManualRowRepository.findById(manualRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));

        if (request.getRouteName() != null) {
            manualRow.setRouteName(trimToNull(request.getRouteName()));
        }
        if (request.getDriverName() != null) {
            manualRow.setDriverName(trimToNull(request.getDriverName()));
        }
        if (request.getDriverLastName() != null) {
            manualRow.setDriverLastName(trimToNull(request.getDriverLastName()));
        }
        if (request.getUnitType() != null) {
            manualRow.setUnitType(trimToNull(request.getUnitType()));
        }
        if (request.getSecondaryValue() != null) {
            manualRow.setSecondaryValue(trimToNull(request.getSecondaryValue()));
        }
        if (request.getExtraRow() != null) {
            manualRow.setExtraRow(request.getExtraRow());
        }
        if (request.getSortOrder() != null) {
            manualRow.setSortOrder(request.getSortOrder());
        }
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        FormatWeekManualRow saved = formatWeekManualRowRepository.save(manualRow);
        return new FormatWeekManualRowDTO(
                saved.getManualRowId(),
                saved.getRouteName(),
                saved.getDriverName(),
                saved.getDriverLastName(),
                saved.getUnitType(),
                saved.getSecondaryValue(),
                saved.getExtraRow(),
                saved.getSortOrder()
        );
    }

    @Override
    @Transactional
    public void deleteManualRow(Long manualRowId) {
        if (manualRowId == null) {
            throw new RuntimeException("manualRowId is required");
        }

        FormatWeekManualRow manualRow = formatWeekManualRowRepository.findById(manualRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));
        deleteManualRowsAndCounts(List.of(manualRow));
    }

    @Override
    @Transactional
    public void updateFormatWeekStatus(
            Long plantId,
            Long formatTypeId,
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            Long shiftId,
            String dayKey,
            String status,
            Long userId,
            List<Long> recipientUserIds
    ) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }
        if (shiftId == null) {
            throw new RuntimeException("shiftId is required");
        }
        if (dayKey != null && dayKey.isBlank()) {
            dayKey = null;
        }
        if (status == null || status.isBlank()) {
            throw new RuntimeException("status is required");
        }

        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status");
        }

        if (cascadaStatus != CascadaStatus.SENT && cascadaStatus != CascadaStatus.DELETED) {
            throw new RuntimeException("status must be SENT or DELETED");
        }

        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                weekDate,
                weekStartDate,
                weekEndDate,
                weekNumber
        );

        List<FormatWeek> weeks = findFormatWeeksByRequestedWeek(plantId, formatTypeId, weekDate, shiftId);

        if (weeks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (FormatWeek week : weeks) {
            if (cascadaStatus == CascadaStatus.SENT) {
                if (week.getSentAt() == null) {
                    week.setSentAt(now);
                }
                if (week.getSentByUserId() == null) {
                    week.setSentByUserId(userId);
                }
            }
            week.setWeekStartDate(weekMetadata.getWeekStartDate());
            week.setWeekEndDate(weekMetadata.getWeekEndDate());
            week.setWeekNumber(weekMetadata.getWeekNumber());
            week.setStatus(cascadaStatus);
            week.setUpdatedAt(now);
            week.setUpdatedByUserId(userId);
        }
        formatWeekRepository.saveAll(weeks);

        if (cascadaStatus == CascadaStatus.SENT) {
            if (recipientUserIds == null || recipientUserIds.isEmpty()) {
                throw new RuntimeException("recipientUserIds is required");
            }

            cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndCascadaType(
                    plantId,
                    weekMetadata.getWeekStartDate(),
                    shiftId.toString(),
                    CascadaType.CUSTOM
            );

            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));

            List<CascadaRecipient> recipients = new java.util.ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(weekMetadata.getWeekStartDate());
                recipient.setShiftId(shiftId.toString());
                recipient.setDayKey(dayKey);
                recipient.setCascadaType(CascadaType.CUSTOM);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);
            deferPublishFormatInboxMessagesForStatus(
                    CascadaStatus.SENT.name(),
                    plantId,
                    weekMetadata.getWeekStartDate(),
                    recipientUserIds
            );
            return;
        }

        if (cascadaStatus == CascadaStatus.DELETED) {
            deferPublishFormatInboxMessagesForStatus(
                    CascadaStatus.DELETED.name(),
                    plantId,
                    weekMetadata.getWeekStartDate(),
                    recipientUserIdsForCustom(plantId, weekMetadata.getWeekStartDate(), shiftId.toString(), dayKey)
            );
            return;
        }
    }

    @Override
    public List<CascadaSummaryDTO> getFormatWeekSummaries(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long recipientUserId
    ) {
        String effectiveStatus = (status == null || status.isBlank()) ? CascadaStatus.SENT.name() : status;
        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(effectiveStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + effectiveStatus);
        }

        LocalDate effectiveRequestedWeekStartDate = weekDate == null
                ? null
                : WeekMetadataResolver.resolve(weekDate, null, null, null).getWeekStartDate();

        List<FormatWeek> weeks;
        if (plantId != null) {
            weeks = formatWeekRepository.findByStatusAndPlantPlantId(cascadaStatus, plantId);
        } else {
            weeks = formatWeekRepository.findByStatus(cascadaStatus);
        }

        if (effectiveRequestedWeekStartDate != null) {
            weeks = weeks.stream()
                    .filter(week -> resolveWeekStartDate(week).equals(effectiveRequestedWeekStartDate))
                    .toList();
        }

        if (recipientUserId != null) {
            List<CascadaRecipient> recipients = cascadaRecipientRepository
                    .findByRecipientUserIdAndCascadaType(recipientUserId, CascadaType.CUSTOM);
            Set<String> allowedKeys = recipients.stream()
                    .map(r -> r.getPlant().getPlantId() + "|" + r.getWeekStartDate() + "|" + r.getShiftId())
                    .collect(Collectors.toSet());

            weeks = weeks.stream()
                    .filter(week -> {
                        String shiftKey = week.getShift() == null ? null : week.getShift().getShiftId().toString();
                        if (shiftKey == null) {
                            return false;
                        }
                        return allowedKeys.contains(
                                week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week) + "|" + shiftKey
                        );
                    })
                    .toList();
        }

        Map<String, List<FormatWeek>> grouped = weeks.stream()
                .collect(Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + resolveWeekStartDate(week)
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<FormatWeek> groupWeeks = entry.getValue();
            FormatWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(FormatWeek::getSentAt))
                    .orElseGet(() -> groupWeeks.stream()
                            .filter(d -> d.getUpdatedAt() != null)
                            .max(java.util.Comparator.comparing(FormatWeek::getUpdatedAt))
                            .orElse(groupWeeks.get(0)));

            LocalDate effectiveWeekStartDate = resolveWeekStartDate(latest);
            Set<String> shiftIds = groupWeeks.stream()
                    .map(week -> week.getShift() == null ? null : week.getShift().getShiftId())
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .sorted(this::compareShiftIds)
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

            Set<String> dayKeys = new java.util.LinkedHashSet<>();
            for (FormatWeek week : groupWeeks) {
                if (week.getCells() == null) {
                    continue;
                }
                for (FormatWeekCell cell : week.getCells()) {
                    if (cell.getDayOfWeek() != null) {
                        dayKeys.add(cell.getDayOfWeek());
                    }
                }
            }

            String sentBy = null;
            Long sentById = latest.getSentByUserId() != null ? latest.getSentByUserId() : latest.getUpdatedByUserId();
            if (sentById != null) {
                User user = userRepository.findById(sentById).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getFormatWeekId(),
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
                    latest.getSentAt() != null ? latest.getSentAt() : latest.getUpdatedAt()
            ));
        }

        summaries.sort(java.util.Comparator.comparing(
                CascadaSummaryDTO::getSentAt,
                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())
        ).reversed());

        return summaries;
    }

    @Override
    public FormatWeekSchemaDTO getFormatWeekSchema(Long formatTypeId) {
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }

        FormatType formatType = formatTypeRepository.findById(formatTypeId)
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        List<String> baseColumns = buildBaseColumns(formatType);
        Map<String, List<FormatTurnConfig>> grouped = groupTurnConfigs(
                formatTurnConfigRepository.findByFormatTypeFormatTypeId(formatTypeId)
        );

        Map<String, List<FormatWeekTurnDTO>> days = new LinkedHashMap<>();
        for (var entry : grouped.entrySet()) {
            List<FormatWeekTurnDTO> turns = entry.getValue().stream()
                    .map(config -> new FormatWeekTurnDTO(
                            config.getTurnConfigId(),
                            config.getTurnName(),
                            config.getSortOrder()
                    ))
                    .toList();
            days.put(entry.getKey(), turns);
        }

        return new FormatWeekSchemaDTO(formatTypeId, baseColumns, days, List.of());
    }

    private FormatWeekRowDTO toRowDTO(FormatWeek week) {
        List<FormatWeekCellDTO> cells = week.getCells() == null
                ? List.of()
                : week.getCells().stream()
                        .map(cell -> new FormatWeekCellDTO(
                                cell.getTurnConfig().getTurnConfigId(),
                                cell.getDayOfWeek(),
                                cell.getTripCount()
                        ))
                        .toList();

        FormatWeekManualRow manualRow = week.getManualRow();
        return new FormatWeekRowDTO(
                week.getFormatWeekId(),
                manualRow == null ? null : manualRow.getManualRowId(),
                week.getRoute() == null ? null : week.getRoute().getRouteId(),
                manualRow != null
                        ? manualRow.getRouteName()
                        : week.getRouteNameOverride() != null
                                ? week.getRouteNameOverride()
                                : week.getRoute() == null ? null : week.getRoute().getRouteName(),
                week.getDriver() == null ? null : week.getDriver().getDriverId(),
                manualRow != null
                        ? manualRow.getDriverName()
                        : week.getDriverNameOverride() != null
                                ? week.getDriverNameOverride()
                                : week.getDriver() == null ? null : week.getDriver().getDriverName(),
                manualRow != null
                        ? manualRow.getDriverLastName()
                        : week.getDriverLastNameOverride() != null
                                ? week.getDriverLastNameOverride()
                                : week.getDriver() == null ? null : week.getDriver().getLastName(),
                manualRow != null ? manualRow.getUnitType() : week.getUnitType(),
                manualRow != null ? manualRow.getSecondaryValue() : week.getSecondaryValue(),
                manualRow != null ? Boolean.TRUE : week.getRoute() == null && week.getDriver() == null,
                cells,
                null
        );
    }

    private boolean usesDriver(FormatType formatType) {
        return formatType.getName() != null
                && formatType.getName().trim().equalsIgnoreCase("TPI_PLANTA_MX2_3");
    }

    private List<String> buildBaseColumns(FormatType formatType) {
        List<String> columns = new ArrayList<>();
        boolean usesDriver = usesDriver(formatType);
        if (usesDriver) {
            columns.add("Chofer");
            columns.add("Ruta");
        } else {
            columns.add("Ruta");
        }
        if (formatType.getSecondaryColumn() != null && !formatType.getSecondaryColumn().isBlank()) {
            columns.add("Recorrido");
        }
        if (Boolean.TRUE.equals(formatType.getIncludesUnitType())) {
            columns.add("Tipo de unidad");
        }
        return columns;
    }

    private Map<String, List<FormatTurnConfig>> groupTurnConfigs(List<FormatTurnConfig> configs) {
        Map<String, List<FormatTurnConfig>> grouped = new LinkedHashMap<>();
        for (String dayKey : List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom")) {
            grouped.put(dayKey, new ArrayList<>());
        }
        for (FormatTurnConfig config : configs) {
            if (config.getDayOfWeek() == null) {
                continue;
            }
            grouped.computeIfAbsent(config.getDayOfWeek(), k -> new ArrayList<>()).add(config);
        }
        for (List<FormatTurnConfig> list : grouped.values()) {
            list.sort(java.util.Comparator.comparingInt(FormatTurnConfig::getSortOrder));
        }
        return grouped;
    }

    private List<FormatWeekRowDTO> buildRouteRows(
            Long plantId,
            FormatType formatType,
            List<FormatTurnConfig> turnConfigs
    ) {
        List<Route> routes = routeRepository.findByPlantPlantId(plantId);
        return routes.stream()
                .map(route -> new FormatWeekRowDTO(
                        null,
                        null,
                        route.getRouteId(),
                        route.getRouteName(),
                        null,
                        null,
                        null,
                        null,
                        formatType.getSecondaryColumn() == null ? null : route.getLocation(),
                        Boolean.FALSE,
                        buildEmptyCells(turnConfigs),
                        null
                ))
                .toList();
    }

    private List<FormatWeekRowDTO> buildDriverRows(
            Long plantId,
            Long shiftId,
            FormatType formatType,
            List<FormatTurnConfig> turnConfigs
    ) {
        List<Driver> drivers = driverRepository.findByShiftsShiftId(shiftId);
        List<FormatWeekRowDTO> rows = new ArrayList<>();

        for (Driver driver : drivers) {
            java.util.Optional<com.example.backend_sistema_LPE.model.DriverPlantAssignment> assignmentOpt =
                    driverPlantAssignmentRepository.findByDriverDriverIdAndPlantPlantId(
                    driver.getDriverId(),
                    plantId
            );
            if (assignmentOpt.isEmpty()) {
                continue;
            }
            Route route = assignmentOpt.get().getRoute();
            rows.add(new FormatWeekRowDTO(
                    null,
                    null,
                    route == null ? null : route.getRouteId(),
                    route == null ? null : route.getRouteName(),
                    driver.getDriverId(),
                    driver.getDriverName(),
                    driver.getLastName(),
                    null,
                    formatType.getSecondaryColumn() == null ? null : route == null ? null : route.getLocation(),
                    Boolean.FALSE,
                    buildEmptyCells(turnConfigs),
                    null
            ));
        }

        return rows;
    }

    private List<FormatWeekCellDTO> buildEmptyCells(List<FormatTurnConfig> turnConfigs) {
        List<FormatWeekCellDTO> cells = new ArrayList<>();
        for (FormatTurnConfig config : turnConfigs) {
            cells.add(new FormatWeekCellDTO(
                    config.getTurnConfigId(),
                    config.getDayOfWeek(),
                    0
            ));
        }
        return cells;
    }

    private List<FormatWeekRowDTO> buildManualRows(
            List<FormatWeekManualRow> manualRows,
            List<FormatTurnConfig> turnConfigs
    ) {
        if (manualRows == null || manualRows.isEmpty()) {
            return List.of();
        }
        return manualRows.stream()
                .map(row -> new FormatWeekRowDTO(
                        null,
                        row.getManualRowId(),
                        null,
                        row.getRouteName(),
                        null,
                        row.getDriverName(),
                        row.getDriverLastName(),
                        row.getUnitType(),
                        row.getSecondaryValue(),
                        Boolean.TRUE.equals(row.getExtraRow()),
                        buildEmptyCells(turnConfigs),
                        null
                ))
                .toList();
    }

    private List<FormatWeekRowDTO> mergeBaseRows(
            List<FormatWeekRowDTO> baseRows,
            List<FormatWeekRowDTO> savedRows,
            boolean usesDriver
    ) {
        Map<String, FormatWeekRowDTO> baseByKey = baseRows.stream()
                .map(row -> Map.entry(rowKey(row, usesDriver), row))
                .filter(entry -> entry.getKey() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<FormatWeekRowDTO> merged = new ArrayList<>(baseRows);

        for (FormatWeekRowDTO saved : savedRows) {
            String key = rowKey(saved, usesDriver);
            if (key == null) {
                merged.add(saved);
                continue;
            }
            FormatWeekRowDTO base = baseByKey.get(key);
            if (base == null) {
                merged.add(saved);
                continue;
            }
            mergeRow(base, saved);
        }

        return merged;
    }

    private String rowKey(FormatWeekRowDTO row, boolean usesDriver) {
        if (row.getManualRowId() != null) {
            return "M:" + row.getManualRowId();
        }
        if (row.getExtraRow() != null && row.getExtraRow()) {
            String label = row.getSecondaryValue();
            if (label == null || label.isBlank()) {
                label = "EMPTY";
            }
            return "X:" + label;
        }
        if (usesDriver) {
            return row.getDriverId() == null ? null : "D:" + row.getDriverId();
        }
        return row.getRouteId() == null ? null : "R:" + row.getRouteId();
    }

    private void mergeRow(FormatWeekRowDTO base, FormatWeekRowDTO saved) {
        if (base.getFormatWeekId() == null && saved.getFormatWeekId() != null) {
            base.setFormatWeekId(saved.getFormatWeekId());
        }
        if (base.getManualRowId() == null && saved.getManualRowId() != null) {
            base.setManualRowId(saved.getManualRowId());
        }
        if (saved.getRouteName() != null) {
            base.setRouteName(saved.getRouteName());
        }
        if (saved.getDriverName() != null) {
            base.setDriverName(saved.getDriverName());
        }
        if (saved.getDriverLastName() != null) {
            base.setDriverLastName(saved.getDriverLastName());
        }
        if (saved.getUnitType() != null) {
            base.setUnitType(saved.getUnitType());
        }
        if (saved.getSecondaryValue() != null) {
            base.setSecondaryValue(saved.getSecondaryValue());
        }

        Map<Long, FormatWeekCellDTO> baseCells = (base.getCells() == null ? List.<FormatWeekCellDTO>of() : base.getCells())
                .stream()
                .filter(cell -> cell.getTurnConfigId() != null)
                .collect(Collectors.toMap(FormatWeekCellDTO::getTurnConfigId, c -> c, (a, b) -> a));

        List<FormatWeekCellDTO> savedCells = saved.getCells() == null ? List.of() : saved.getCells();
        for (FormatWeekCellDTO savedCell : savedCells) {
            if (savedCell.getTurnConfigId() == null) {
                continue;
            }
            FormatWeekCellDTO baseCell = baseCells.get(savedCell.getTurnConfigId());
            if (baseCell == null) {
                if (base.getCells() == null) {
                    base.setCells(new ArrayList<>());
                }
                base.getCells().add(savedCell);
                continue;
            }
            baseCell.setTripCount(savedCell.getTripCount());
        }
    }

    private Map<String, FormatWeekRowDTO> resolveWeeklyOverrideRows(
            List<FormatWeek> weeklyWeeks,
            boolean usesDriver
    ) {
        if (weeklyWeeks == null || weeklyWeeks.isEmpty()) {
            return Map.of();
        }

        List<FormatWeek> orderedWeeks = new ArrayList<>(weeklyWeeks);
        orderedWeeks.sort(java.util.Comparator
                .comparing(this::weeklyOverrideTimestamp, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder()))
                .thenComparing(FormatWeek::getFormatWeekId, java.util.Comparator.nullsFirst(java.util.Comparator.naturalOrder())));

        Map<String, FormatWeekRowDTO> overrides = new LinkedHashMap<>();
        for (FormatWeek week : orderedWeeks) {
            FormatWeekRowDTO row = toWeeklyOverrideRowDTO(week);
            String key = rowKey(row, usesDriver);
            if (key == null) {
                continue;
            }
            FormatWeekRowDTO existing = overrides.get(key);
            if (existing == null) {
                overrides.put(key, row);
                continue;
            }
            mergeWeeklyBaseOverrideRow(existing, row);
        }
        return overrides;
    }

    private LocalDateTime weeklyOverrideTimestamp(FormatWeek week) {
        if (week.getUpdatedAt() != null) {
            return week.getUpdatedAt();
        }
        if (week.getSentAt() != null) {
            return week.getSentAt();
        }
        return null;
    }

    private void applyWeeklyBaseOverrides(FormatWeekRowDTO target, FormatWeekRowDTO source) {
        if (target == null || source == null) {
            return;
        }
        if (source.getRouteName() != null) {
            target.setRouteName(source.getRouteName());
        }
        if (source.getDriverName() != null) {
            target.setDriverName(source.getDriverName());
        }
        if (source.getDriverLastName() != null) {
            target.setDriverLastName(source.getDriverLastName());
        }
        if (source.getUnitType() != null) {
            target.setUnitType(source.getUnitType());
        }
        if (source.getSecondaryValue() != null) {
            target.setSecondaryValue(source.getSecondaryValue());
        }
    }

    private FormatWeekRowDTO toWeeklyOverrideRowDTO(FormatWeek week) {
        FormatWeekManualRow manualRow = week.getManualRow();
        boolean manual = manualRow != null;
        return new FormatWeekRowDTO(
                week.getFormatWeekId(),
                manual ? manualRow.getManualRowId() : null,
                week.getRoute() == null ? null : week.getRoute().getRouteId(),
                manual ? manualRow.getRouteName() : trimToNull(week.getRouteNameOverride()),
                week.getDriver() == null ? null : week.getDriver().getDriverId(),
                manual ? manualRow.getDriverName() : trimToNull(week.getDriverNameOverride()),
                manual ? manualRow.getDriverLastName() : trimToNull(week.getDriverLastNameOverride()),
                manual ? manualRow.getUnitType() : trimToNull(week.getUnitType()),
                manual ? manualRow.getSecondaryValue() : trimToNull(week.getSecondaryValue()),
                manual ? Boolean.TRUE : week.getRoute() == null && week.getDriver() == null,
                List.of(),
                null
        );
    }

    private void mergeWeeklyBaseOverrideRow(FormatWeekRowDTO target, FormatWeekRowDTO source) {
        if (target == null || source == null) {
            return;
        }
        if (source.getRouteName() != null) {
            target.setRouteName(source.getRouteName());
        }
        if (source.getDriverName() != null) {
            target.setDriverName(source.getDriverName());
        }
        if (source.getDriverLastName() != null) {
            target.setDriverLastName(source.getDriverLastName());
        }
        if (source.getUnitType() != null) {
            target.setUnitType(source.getUnitType());
        }
        if (source.getSecondaryValue() != null) {
            target.setSecondaryValue(source.getSecondaryValue());
        }
    }

    private Map<Long, Long> buildShiftIdByTurnConfigId(
            Long plantId,
            Long formatTypeId,
            List<FormatTurnConfig> turnConfigs
    ) {
        if (plantId == null || formatTypeId == null || turnConfigs == null || turnConfigs.isEmpty()) {
            return Map.of();
        }

        Map<String, Long> shiftByDayAndTurn = shiftFormatTurnMapRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeId(plantId, formatTypeId)
                .stream()
                .collect(Collectors.toMap(
                        map -> normalizeTurnConfigKey(map.getDayOfWeek(), map.getTurnName()),
                        map -> map.getShift().getShiftId(),
                        (a, b) -> a
                ));

        Map<Long, Long> shiftIdByTurnConfigId = new LinkedHashMap<>();
        for (FormatTurnConfig config : turnConfigs) {
            shiftIdByTurnConfigId.put(
                    config.getTurnConfigId(),
                    shiftByDayAndTurn.get(normalizeTurnConfigKey(config.getDayOfWeek(), config.getTurnName()))
            );
        }
        return shiftIdByTurnConfigId;
    }

    private List<FormatTurnConfig> filterTurnConfigsForShift(
            List<FormatTurnConfig> turnConfigs,
            Map<Long, Long> shiftIdByTurnConfigId,
            Long shiftId
    ) {
        if (shiftId == null || turnConfigs == null || turnConfigs.isEmpty()) {
            return turnConfigs == null ? List.of() : turnConfigs;
        }
        return turnConfigs.stream()
                .filter(config -> {
                    Long mappedShiftId = shiftIdByTurnConfigId.get(config.getTurnConfigId());
                    if (mappedShiftId == null) {
                        return isFormatLevelColumn(config.getTurnName());
                    }
                    return shiftId.equals(mappedShiftId);
                })
                .toList();
    }

    private FormatWeekRowDTO filterRowCellsByShift(
            FormatWeekRowDTO row,
            Map<Long, Long> shiftIdByTurnConfigId,
            Map<Long, FormatTurnConfig> turnConfigById,
            Long shiftId
    ) {
        if (row == null || shiftId == null || row.getCells() == null || row.getCells().isEmpty()) {
            return row;
        }
        row.setCells(row.getCells().stream()
                .filter(cell -> cell.getTurnConfigId() != null)
                .filter(cell -> {
                    Long mappedShiftId = shiftIdByTurnConfigId.get(cell.getTurnConfigId());
                    if (mappedShiftId == null) {
                        FormatTurnConfig config = turnConfigById.get(cell.getTurnConfigId());
                        return config != null && isFormatLevelColumn(config.getTurnName());
                    }
                    return shiftId.equals(mappedShiftId);
                })
                .toList());
        return row;
    }

    private String normalizeTurnConfigKey(String dayOfWeek, String turnName) {
        String dayKey = dayOfWeek == null ? "" : dayOfWeek.trim().toLowerCase();
        String normalizedTurnName = turnName == null ? "" : turnName.trim().toLowerCase();
        return dayKey + "|" + normalizedTurnName;
    }

    private boolean isFormatLevelColumn(String turnName) {
        if (turnName == null) {
            return false;
        }
        return turnName.trim().toUpperCase().startsWith("TE");
    }

    private void enrichRowTotals(List<FormatWeekRowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (FormatWeekRowDTO row : rows) {
            List<FormatWeekCellDTO> cells = row.getCells() == null ? List.of() : row.getCells();
            int rowTotal = cells.stream()
                    .mapToInt(cell -> cell.getTripCount() == null ? 0 : cell.getTripCount())
                    .sum();
            row.setRowTotal(rowTotal);
        }
    }

    private FormatWeekTotalsDTO buildTotals(List<FormatWeekRowDTO> rows, List<FormatTurnConfig> turnConfigs) {
        Map<String, Map<Long, Integer>> byDayAndTurn = new LinkedHashMap<>();
        Map<String, Integer> byDay = new LinkedHashMap<>();

        Map<String, List<FormatTurnConfig>> groupedConfigs = groupTurnConfigs(turnConfigs == null ? List.of() : turnConfigs);
        for (var entry : groupedConfigs.entrySet()) {
            Map<Long, Integer> turnTotals = new LinkedHashMap<>();
            for (FormatTurnConfig config : entry.getValue()) {
                turnTotals.put(config.getTurnConfigId(), 0);
            }
            byDayAndTurn.put(entry.getKey(), turnTotals);
            byDay.put(entry.getKey(), 0);
        }

        int weekTotal = 0;
        if (rows != null) {
            for (FormatWeekRowDTO row : rows) {
                List<FormatWeekCellDTO> cells = row.getCells() == null ? List.of() : row.getCells();
                for (FormatWeekCellDTO cell : cells) {
                    if (cell.getDayOfWeek() == null || cell.getTurnConfigId() == null) {
                        continue;
                    }
                    int tripCount = cell.getTripCount() == null ? 0 : cell.getTripCount();
                    Map<Long, Integer> turnTotals = byDayAndTurn.computeIfAbsent(cell.getDayOfWeek(), key -> new LinkedHashMap<>());
                    turnTotals.put(cell.getTurnConfigId(), turnTotals.getOrDefault(cell.getTurnConfigId(), 0) + tripCount);
                    byDay.put(cell.getDayOfWeek(), byDay.getOrDefault(cell.getDayOfWeek(), 0) + tripCount);
                    weekTotal += tripCount;
                }
            }
        }

        return new FormatWeekTotalsDTO(byDayAndTurn, byDay, weekTotal);
    }

    private List<FormatWeekManualRow> saveManualRows(
            FormatWeekSaveRequestDTO request,
            Plant plant,
            FormatType formatType,
            Long userId
    ) {
        List<FormatWeekManualRow> existing = new ArrayList<>(formatWeekManualRowRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateOrderBySortOrderAscManualRowIdAsc(
                        request.getPlantId(),
                        request.getFormatTypeId(),
                        request.getWeekDate()
                ));

        boolean explicitManualRows = request.getManualRows() != null;
        List<FormatWeekManualRowDTO> requestedManualRows = request.getManualRows();
        if (requestedManualRows == null) {
            boolean hasNewManualRowsInPayload = request.getRows() != null && request.getRows().stream()
                    .filter(this::isManualRow)
                    .anyMatch(row -> row.getManualRowId() == null);
            if (hasNewManualRowsInPayload) {
                requestedManualRows = inferManualRowsFromShiftPayload(request.getRows());
            } else {
                return existing;
            }
        }

        if (requestedManualRows == null) {
            return existing;
        }
        if (requestedManualRows.isEmpty()) {
            if (explicitManualRows) {
                deleteManualRowsAndCounts(existing);
                return List.of();
            }
            return existing;
        }

        Map<Long, FormatWeekManualRow> existingById = existing.stream()
                .filter(row -> row.getManualRowId() != null)
                .collect(Collectors.toMap(FormatWeekManualRow::getManualRowId, row -> row, (a, b) -> a, LinkedHashMap::new));

        List<FormatWeekManualRow> persisted = new ArrayList<>();
        int sequence = 0;
        LocalDateTime now = LocalDateTime.now();
        for (FormatWeekManualRowDTO dto : requestedManualRows) {
            FormatWeekManualRow row = dto.getManualRowId() == null
                    ? new FormatWeekManualRow()
                    : existingById.getOrDefault(dto.getManualRowId(), new FormatWeekManualRow());
            row.setPlant(plant);
            row.setFormatType(formatType);
            row.setWeekDate(request.getWeekDate());
            row.setRouteName(trimToNull(dto.getRouteName()));
            row.setDriverName(trimToNull(dto.getDriverName()));
            row.setDriverLastName(trimToNull(dto.getDriverLastName()));
            row.setUnitType(trimToNull(dto.getUnitType()));
            row.setSecondaryValue(trimToNull(dto.getSecondaryValue()));
            row.setExtraRow(dto.getExtraRow() == null ? Boolean.TRUE : dto.getExtraRow());
            row.setSortOrder(dto.getSortOrder() == null ? sequence : dto.getSortOrder());
            row.setUpdatedAt(now);
            row.setUpdatedByUserId(userId);
            persisted.add(formatWeekManualRowRepository.save(row));
            sequence++;
        }

        if (request.getManualRows() != null) {
            Set<Long> keptIds = persisted.stream()
                    .map(FormatWeekManualRow::getManualRowId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            List<FormatWeekManualRow> toDelete = existing.stream()
                    .filter(row -> row.getManualRowId() != null && !keptIds.contains(row.getManualRowId()))
                    .toList();
            if (!toDelete.isEmpty()) {
                deleteManualRowsAndCounts(toDelete);
            }
        }

        return formatWeekManualRowRepository
                .findByPlantPlantIdAndFormatTypeFormatTypeIdAndWeekDateOrderBySortOrderAscManualRowIdAsc(
                        request.getPlantId(),
                        request.getFormatTypeId(),
                        request.getWeekDate()
                );
    }

    private List<FormatWeekManualRowDTO> inferManualRowsFromShiftPayload(List<FormatWeekRowDTO> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<FormatWeekManualRowDTO> manualRows = new ArrayList<>();
        int sequence = 0;
        for (FormatWeekRowDTO row : rows) {
            if (!isManualRow(row)) {
                continue;
            }
            manualRows.add(new FormatWeekManualRowDTO(
                    row.getManualRowId(),
                    row.getRouteName(),
                    row.getDriverName(),
                    row.getDriverLastName(),
                    row.getUnitType(),
                    row.getSecondaryValue(),
                    row.getExtraRow(),
                    sequence++
            ));
        }
        return manualRows;
    }

    private FormatWeekManualRow resolveManualRow(FormatWeekRowDTO row, List<FormatWeekManualRow> persistedManualRows) {
        if (!isManualRow(row)) {
            return null;
        }
        if (row.getManualRowId() != null) {
            if (persistedManualRows != null && !persistedManualRows.isEmpty()) {
                FormatWeekManualRow persisted = persistedManualRows.stream()
                        .filter(manualRow -> row.getManualRowId().equals(manualRow.getManualRowId()))
                        .findFirst()
                        .orElse(null);
                if (persisted != null) {
                    return persisted;
                }
            }
            return formatWeekManualRowRepository.findById(row.getManualRowId()).orElse(null);
        }
        if (persistedManualRows == null || persistedManualRows.isEmpty()) {
            return null;
        }
        String signature = manualRowSignature(
                row.getRouteName(),
                row.getDriverName(),
                row.getDriverLastName(),
                row.getUnitType(),
                row.getSecondaryValue()
        );
        return persistedManualRows.stream()
                .filter(manualRow -> signature.equals(manualRowSignature(
                        manualRow.getRouteName(),
                        manualRow.getDriverName(),
                        manualRow.getDriverLastName(),
                        manualRow.getUnitType(),
                        manualRow.getSecondaryValue()
                )))
                .findFirst()
                .orElse(null);
    }

    private boolean isManualRow(FormatWeekRowDTO row) {
        if (row == null) {
            return false;
        }
        if (row.getManualRowId() != null) {
            return true;
        }
        return row.getRouteId() == null && row.getDriverId() == null;
    }

    private String manualRowSignature(
            String routeName,
            String driverName,
            String driverLastName,
            String unitType,
            String secondaryValue
    ) {
        return String.join("|",
                normalizeSignature(routeName),
                normalizeSignature(driverName),
                normalizeSignature(driverLastName),
                normalizeSignature(unitType),
                normalizeSignature(secondaryValue)
        );
    }

    private String normalizeSignature(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private void deleteManualRowsAndCounts(List<FormatWeekManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return;
        }
        List<Long> manualRowIds = manualRows.stream()
                .map(FormatWeekManualRow::getManualRowId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!manualRowIds.isEmpty()) {
            List<FormatWeek> weeks = formatWeekRepository.findByManualRowManualRowIdIn(manualRowIds);
            if (!weeks.isEmpty()) {
                formatWeekRepository.deleteAll(weeks);
            }
        }
        formatWeekManualRowRepository.deleteAll(manualRows);
    }

    private String stableCascadaId(Long plantId, LocalDate weekDate) {
        return "custom-" + plantId + "-" + weekDate;
    }

    private List<FormatWeek> findFormatWeeksByRequestedWeek(
            Long plantId,
            Long formatTypeId,
            LocalDate requestedWeekDate,
            Long shiftId
    ) {
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                requestedWeekDate,
                null,
                null,
                null
        );
        LocalDate effectiveWeekStartDate = weekMetadata.getWeekStartDate();

        List<FormatWeek> weeks = shiftId == null
                ? formatWeekRepository.findByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
                        plantId,
                        effectiveWeekStartDate,
                        formatTypeId
                )
                : formatWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
                        plantId,
                        effectiveWeekStartDate,
                        shiftId,
                        formatTypeId
                );

        if (!weeks.isEmpty() || requestedWeekDate.equals(effectiveWeekStartDate)) {
            return weeks;
        }

        return shiftId == null
                ? formatWeekRepository.findByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
                        plantId,
                        requestedWeekDate,
                        formatTypeId
                )
                : formatWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
                        plantId,
                        requestedWeekDate,
                        shiftId,
                        formatTypeId
                );
    }

    private LocalDate resolveWeekStartDate(FormatWeek week) {
        return week.getWeekStartDate() != null
                ? week.getWeekStartDate()
                : WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekStartDate();
    }

    private LocalDate resolveWeekEndDate(FormatWeek week) {
        return week.getWeekEndDate() != null
                ? week.getWeekEndDate()
                : WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekEndDate();
    }

    private Integer resolveWeekNumber(FormatWeek week) {
        return week.getWeekNumber() != null
                ? week.getWeekNumber()
                : WeekMetadataResolver.resolve(week.getWeekDate(), null, null, null).getWeekNumber();
    }

    private Set<String> orderDayKeys(Set<String> dayKeys) {
        return dayKeys.stream()
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparingInt(this::dayOrder))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
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

    private List<Long> recipientUserIdsForCustom(
            Long plantId,
            LocalDate weekDate,
            String shiftId,
            String dayKey
    ) {
        if (dayKey == null || dayKey.isBlank()) {
            return cascadaRecipientRepository.findRecipientUserIdsByShift(
                    plantId,
                    weekDate,
                    shiftId,
                    CascadaType.CUSTOM
            );
        }
        return cascadaRecipientRepository.findRecipientUserIdsByDayKey(
                plantId,
                weekDate,
                shiftId,
                dayKey,
                CascadaType.CUSTOM
        );
    }

    private void publishFormatInboxMessagesForStatus(
            String status,
            Long plantId,
            LocalDate weekDate,
            List<Long> recipientUserIds
    ) {
        if (recipientUserIds == null || recipientUserIds.isEmpty()) {
            return;
        }

        for (Long recipientUserId : recipientUserIds) {
            List<CascadaSummaryDTO> summaries = getFormatWeekSummaries(
                    status,
                    plantId,
                    weekDate,
                    recipientUserId
            );
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

    private void deferPublishFormatInboxMessagesForStatus(
            String status,
            Long plantId,
            LocalDate weekDate,
            List<Long> recipientUserIds
    ) {
        Runnable publisher = () -> publishFormatInboxMessagesForStatus(status, plantId, weekDate, recipientUserIds);
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
}
