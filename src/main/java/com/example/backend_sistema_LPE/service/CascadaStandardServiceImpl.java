package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaStandardManualRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekItemDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.CreateCascadaStandardManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.dto.StandardWeeklyResponseDTO;
import com.example.backend_sistema_LPE.dto.StandardWeeklyRowDTO;
import com.example.backend_sistema_LPE.dto.StandardWeeklyShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateCascadaStandardManualRowRequestDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.enums.CascadaType;
import com.example.backend_sistema_LPE.enums.DriverType;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.CascadaStandardCell;
import com.example.backend_sistema_LPE.model.CascadaStandardManualRow;
import com.example.backend_sistema_LPE.model.CascadaStandardWeek;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.Route;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.CascadaStandardCellRepository;
import com.example.backend_sistema_LPE.repository.CascadaStandardManualRowRepository;
import com.example.backend_sistema_LPE.repository.CascadaStandardWeekRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CascadaStandardServiceImpl implements CascadaStandardService {
    private static final Logger log = LoggerFactory.getLogger(CascadaStandardServiceImpl.class);

    private final CascadaStandardWeekRepository weekRepository;
    private final CascadaStandardCellRepository cellRepository;
    private final CascadaStandardManualRowRepository manualRowRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final PlantRepository plantRepository;
    private final DriverRepository driverRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final RouteRepository routeRepository;
    private final UserRepository userRepository;
    private final ShiftService shiftService;
    private final SimpMessagingTemplate messagingTemplate;

    public CascadaStandardServiceImpl(
            CascadaStandardWeekRepository weekRepository,
            CascadaStandardCellRepository cellRepository,
            CascadaStandardManualRowRepository manualRowRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            PlantRepository plantRepository,
            DriverRepository driverRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            RouteRepository routeRepository,
            UserRepository userRepository,
            ShiftService shiftService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.weekRepository = weekRepository;
        this.cellRepository = cellRepository;
        this.manualRowRepository = manualRowRepository;
        this.cascadaRecipientRepository = cascadaRecipientRepository;
        this.plantRepository = plantRepository;
        this.driverRepository = driverRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.shiftService = shiftService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey, String status) {
        WeekMetadataResolver.ResolvedWeekMetadata requestedWeekMetadata = WeekMetadataResolver.resolve(
                weekStartDate,
                null,
                null,
                null
        );
        LocalDate effectiveWeekStartDate = requestedWeekMetadata.getWeekStartDate();
        CascadaStandardWeek week = weekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(plantId, effectiveWeekStartDate, shiftId)
                .orElse(null);

        List<CascadaStandardManualRow> manualRows = manualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(plantId, effectiveWeekStartDate);
        List<CascadaRowDTO> rowDTOs = buildMergedRows(plantId, week, shiftId, dayKey, manualRows);

        if (week != null && status != null && !status.isBlank()) {
            if (week.getStatus() == null || !week.getStatus().name().equals(status)) {
                rowDTOs = List.of();
            }
        }

        CascadaResponseDTO response = new CascadaResponseDTO();
        response.setPlantId(plantId);
        response.setWeekStartDate(week != null && week.getWeekStartDate() != null ? week.getWeekStartDate() : requestedWeekMetadata.getWeekStartDate());
        response.setWeekEndDate(week != null && week.getWeekEndDate() != null ? week.getWeekEndDate() : requestedWeekMetadata.getWeekEndDate());
        response.setWeekNumber(week != null && week.getWeekNumber() != null
                ? week.getWeekNumber()
                : requestedWeekMetadata.getWeekNumber());
        response.setShiftId(shiftId);
        response.setDayKey(dayKey);
        response.setStatus(week == null ? status : week.getStatus().name());
        response.setManualRows(toManualRowDTOs(manualRows));
        response.setRows(rowDTOs);
        return response;
    }

    @Override
    @Transactional
    public void saveCascada(CascadaSaveRequestDTO request) {
        boolean usesNewFlow = request.getDayKey() != null
                && !request.getDayKey().isBlank()
                && request.getRows() != null;
        boolean usesLegacyFlow = request.getDays() != null && !request.getDays().isEmpty();
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                request.getWeekDate(),
                request.getWeekStartDate(),
                request.getWeekEndDate(),
                request.getWeekNumber()
        );
        request.setWeekStartDate(weekMetadata.getWeekStartDate());
        request.setWeekEndDate(weekMetadata.getWeekEndDate());
        request.setWeekNumber(weekMetadata.getWeekNumber());
        request.setWeekDate(weekMetadata.getWeekStartDate());

        log.info(
                "SAVE standard cascada: plantId={}, weekDate={}, shiftId={}, days={}",
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId(),
                usesNewFlow ? 1 : request.getDays() == null ? 0 : request.getDays().size()
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
        if (usesNewFlow) {
            if (request.getRows().isEmpty()) {
                throw new RuntimeException("rows is required");
            }
        } else if (!usesLegacyFlow) {
            throw new RuntimeException("Se requiere dayKey + rows o days");
        }

        if (usesNewFlow) {
            log.info("New standard flow -> dayKey={}, rows={}", request.getDayKey(), request.getRows().size());
        } else {
            log.info("Days keys recibidos: {}", request.getDays().keySet());
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Map<String, List<CascadaRowDTO>> requestedDays = normalizeDays(request, usesNewFlow);

        List<Long> driverIds = requestedDays.values().stream()
                .filter(java.util.Objects::nonNull)
                .flatMap(list -> list.stream().map(CascadaRowDTO::getDriverId))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Driver> driversById = driverRepository.findAllById(driverIds).stream()
                .collect(Collectors.toMap(Driver::getDriverId, d -> d));

        for (Long driverId : driverIds) {
            if (!driversById.containsKey(driverId)) {
                throw new RuntimeException("Driver not found: " + driverId);
            }
        }

        CascadaStandardWeek week = weekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(
                        plant.getPlantId(),
                        request.getWeekDate(),
                        request.getShiftId()
                )
                .orElse(null);

        LocalDateTime now = LocalDateTime.now();
        if (week == null) {
            week = new CascadaStandardWeek();
            week.setPlant(plant);
            week.setWeekStartDate(request.getWeekDate());
            week.setShiftId(request.getShiftId());
        }
        week.setWeekStartDate(request.getWeekStartDate());
        week.setWeekEndDate(request.getWeekEndDate());
        week.setWeekNumber(request.getWeekNumber());

        week.setStatus(CascadaStatus.DRAFT);
        week.setUpdatedAt(now);
        week.setUpdatedByUserId(null);
        week.setSentAt(null);
        week.setSentByUserId(null);
        week = weekRepository.save(week);

        List<CascadaStandardManualRow> manualRows = saveManualRows(request, plant, now, usesNewFlow);

        if (!requestedDays.isEmpty()) {
            log.info("Days keys normalizados para borrar: {}", requestedDays.keySet());
            cellRepository.deleteByWeekAndDayKeyIn(week, requestedDays.keySet());
            cellRepository.flush();
        }

        List<CascadaStandardCell> cells = new ArrayList<>();
        for (var entry : requestedDays.entrySet()) {
            String dayKey = entry.getKey();
            List<CascadaRowDTO> rows = entry.getValue();
            Map<String, CascadaRowDTO> dedupedRows = new LinkedHashMap<>();
            for (CascadaRowDTO row : rows) {
                if (row == null) {
                    continue;
                }
                String key = rowIdentityKey(row);
                dedupedRows.put(key, row);
            }
            for (CascadaRowDTO row : dedupedRows.values()) {
                CascadaStandardCell cell = new CascadaStandardCell();
                cell.setWeek(week);
                cell.setDayKey(dayKey);
                CascadaStandardManualRow manualRow = resolveManualRow(row, manualRows);
                if (isManualRow(row) && manualRow == null) {
                    throw new RuntimeException("Manual row not found: " + row.getManualStandardRowId());
                }
                if (manualRow != null) {
                    cell.setManualRow(manualRow);
                    cell.setRouteId(manualRow.getRouteId());
                    cell.setDriverNameOverride(normalizeValue(
                            row.getDriverNameOverride() == null ? manualRow.getDriverNameOverride() : row.getDriverNameOverride()
                    ));
                } else {
                    if (row.getDriverId() == null) {
                        continue;
                    }
                    cell.setDriver(driversById.get(row.getDriverId()));
                    cell.setRouteId(row.getRouteId());
                    cell.setDriverNameOverride(normalizeValue(row.getDriverNameOverride()));
                }
                cell.setE(normalizeValue(row.getE()));
                cell.setS(normalizeValue(row.getS()));
                cell.setEte(normalizeValue(row.getEte()));
                cell.setSte(normalizeValue(row.getSte()));
                log.info(
                        "Insertando -> dayKey: {}, driverId: {}, routeId: {}, manualRowId: {}",
                        dayKey,
                        cell.getDriver() == null ? null : cell.getDriver().getDriverId(),
                        cell.getRouteId(),
                        cell.getManualRow() == null ? null : cell.getManualRow().getManualStandardRowId()
                );
                cells.add(cell);
            }
        }
        cellRepository.saveAll(cells);
    }

    @Override
    @Transactional
    public CascadaStandardManualRowDTO createManualRow(CreateCascadaStandardManualRowRequestDTO request, Long userId) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekDate() == null) {
            throw new RuntimeException("weekDate is required");
        }
        LocalDate effectiveWeekDate = WeekMetadataResolver.resolve(
                request.getWeekDate(),
                null,
                null,
                null
        ).getWeekStartDate();

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        List<CascadaStandardManualRow> existing = manualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(
                        request.getPlantId(),
                        effectiveWeekDate
                );

        int nextSortOrder = existing.stream()
                .map(CascadaStandardManualRow::getSortOrder)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .map(value -> value + 1)
                .orElse(existing.size());

        CascadaStandardManualRow manualRow = new CascadaStandardManualRow();
        manualRow.setPlant(plant);
        manualRow.setWeekDate(effectiveWeekDate);
        manualRow.setDriverNameOverride(trimToNull(request.getDriverNameOverride()));
        manualRow.setRouteId(request.getRouteId());
        manualRow.setRouteName(trimToNull(request.getRouteName()));
        manualRow.setSortOrder(request.getSortOrder() == null ? nextSortOrder : request.getSortOrder());
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        CascadaStandardManualRow saved = manualRowRepository.save(manualRow);
        return toManualRowResponse(saved);
    }

    @Override
    @Transactional
    public CascadaStandardManualRowDTO updateManualRow(
            Long manualStandardRowId,
            UpdateCascadaStandardManualRowRequestDTO request,
            Long userId
    ) {
        if (manualStandardRowId == null) {
            throw new RuntimeException("manualStandardRowId is required");
        }

        CascadaStandardManualRow manualRow = manualRowRepository.findById(manualStandardRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));

        if (request.getDriverNameOverride() != null) {
            manualRow.setDriverNameOverride(trimToNull(request.getDriverNameOverride()));
        }
        if (request.getRouteId() != null || request.getRouteName() != null) {
            manualRow.setRouteId(request.getRouteId());
            manualRow.setRouteName(trimToNull(request.getRouteName()));
        }
        if (request.getSortOrder() != null) {
            manualRow.setSortOrder(request.getSortOrder());
        }
        manualRow.setUpdatedAt(LocalDateTime.now());
        manualRow.setUpdatedByUserId(userId);

        CascadaStandardManualRow saved = manualRowRepository.save(manualRow);
        return toManualRowResponse(saved);
    }

    @Override
    @Transactional
    public void deleteManualRow(Long manualStandardRowId) {
        if (manualStandardRowId == null) {
            throw new RuntimeException("manualStandardRowId is required");
        }

        CascadaStandardManualRow manualRow = manualRowRepository.findById(manualStandardRowId)
                .orElseThrow(() -> new RuntimeException("Manual row not found"));
        deleteManualRowsAndCells(List.of(manualRow));
    }

    @Override
    @Transactional
    public void updateCascadaStatus(
            Long plantId,
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            String shiftId,
            String dayKey,
            String status,
            Long userId,
            List<Long> recipientUserIds
    ) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
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

        LocalDate effectiveWeekDate = weekDate != null ? weekDate : weekStartDate;
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                effectiveWeekDate,
                weekStartDate,
                weekEndDate,
                weekNumber
        );
        LocalDate effectiveWeekStartDate = weekMetadata.getWeekStartDate();

        CascadaStandardWeek week = weekRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftId(plantId, effectiveWeekStartDate, shiftId)
                .orElse(null);

        if (week == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (targetStatus == CascadaStatus.SENT) {
            if (recipientUserIds == null || recipientUserIds.isEmpty()) {
                throw new RuntimeException("recipientUserIds is required");
            }
            week.setSentAt(week.getSentAt() == null ? now : week.getSentAt());
            week.setSentByUserId(week.getSentByUserId() == null ? userId : week.getSentByUserId());
        }
        week.setWeekStartDate(effectiveWeekStartDate);
        week.setWeekEndDate(weekMetadata.getWeekEndDate());
        week.setWeekNumber(weekMetadata.getWeekNumber());
        week.setStatus(targetStatus);
        week.setUpdatedAt(now);
        week.setUpdatedByUserId(userId);
        weekRepository.save(week);

        if (targetStatus == CascadaStatus.SENT) {
            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));

            if (dayKey == null) {
                cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndCascadaType(
                        plantId,
                        effectiveWeekStartDate,
                        shiftId,
                        CascadaType.STANDARD
                );
            } else {
                cascadaRecipientRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKeyAndCascadaType(
                        plantId,
                        effectiveWeekStartDate,
                        shiftId,
                        dayKey,
                        CascadaType.STANDARD
                );
            }

            List<CascadaRecipient> recipients = new ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(effectiveWeekStartDate);
                recipient.setShiftId(shiftId);
                recipient.setDayKey(dayKey);
                recipient.setCascadaType(CascadaType.STANDARD);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);

            publishInboxMessagesForStatus(targetStatus.name(), plantId, effectiveWeekStartDate, recipientUserIds);
            return;
        }

        publishInboxMessagesForStatus(
                targetStatus.name(),
                plantId,
                effectiveWeekStartDate,
                recipientUserIdsFor(plantId, effectiveWeekStartDate, shiftId, dayKey)
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
        WeekMetadataResolver.ResolvedWeekMetadata requestedWeekMetadata = WeekMetadataResolver.resolve(
                weekStartDate,
                null,
                null,
                null
        );
        LocalDate effectiveWeekStartDate = requestedWeekMetadata.getWeekStartDate();

        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + status);
        }

        List<CascadaStandardWeek> weeks = weekRepository
                .findByPlantPlantIdAndWeekStartDateAndStatus(plantId, effectiveWeekStartDate, cascadaStatus);
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = weeks.isEmpty()
                ? requestedWeekMetadata
                : WeekMetadataResolver.resolve(
                        weeks.get(0).getWeekStartDate(),
                        weeks.get(0).getWeekStartDate(),
                        weeks.get(0).getWeekEndDate(),
                        weeks.get(0).getWeekNumber()
                );
        Map<String, CascadaStandardWeek> weekByShiftId = weeks.stream()
                .collect(Collectors.toMap(CascadaStandardWeek::getShiftId, week -> week, (a, b) -> a, LinkedHashMap::new));
        List<CascadaStandardManualRow> manualRows = manualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(plantId, effectiveWeekStartDate);
        List<ShiftDTO> shifts = shiftService.getShiftsByPlant(plantId);

        List<CascadaWeekItemDTO> items = new ArrayList<>();
        for (ShiftDTO shift : shifts) {
            CascadaStandardWeek week = weekByShiftId.get(shift.getShiftId().toString());
            if (week == null) {
                continue;
            }
            List<String> dayKeys = shift.getDayKeys() == null ? List.of() : shift.getDayKeys().stream()
                    .filter(java.util.Objects::nonNull)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(key -> !key.isBlank())
                    .toList();
            for (String dayKey : dayKeys) {
                items.add(new CascadaWeekItemDTO(
                        shift.getShiftId(),
                        dayKey,
                        buildMergedRows(plantId, week, shift.getShiftId().toString(), dayKey, manualRows)
                ));
            }
        }

        CascadaWeekTotalsDTO totals = buildWeekTotals(plantId, items);
        return new CascadaWeekResponseDTO(
                plantId,
                weekMetadata.getWeekStartDate(),
                weekMetadata.getWeekEndDate(),
                weekMetadata.getWeekNumber(),
                status,
                items,
                totals
        );
    }

    @Override
    public StandardWeeklyResponseDTO getStandardWeeklyView(Long plantId, LocalDate weekDate, String status) {
        WeekMetadataResolver.ResolvedWeekMetadata requestedWeekMetadata = WeekMetadataResolver.resolve(
                weekDate,
                null,
                null,
                null
        );
        CascadaWeekResponseDTO weekResponse = getWeekCascadas(plantId, requestedWeekMetadata.getWeekStartDate(), status);
        List<CascadaWeekItemDTO> items = weekResponse.getItems();
        List<ShiftDTO> shiftList = shiftService.getShiftsByPlant(plantId);
        Map<Long, StandardWeeklyShiftDTO> shifts = new LinkedHashMap<>();

        for (ShiftDTO shift : shiftList) {
            Map<String, List<StandardWeeklyRowDTO>> days = new LinkedHashMap<>();
            if (shift.getDayKeys() != null) {
                for (String dayKey : shift.getDayKeys()) {
                    if (dayKey == null || dayKey.isBlank()) {
                        continue;
                    }
                    days.put(dayKey.trim().toLowerCase(), new ArrayList<>());
                }
            }
            shifts.put(
                    shift.getShiftId(),
                    new StandardWeeklyShiftDTO(shift.getShiftId(), shift.getShiftName(), days)
            );
        }

        for (CascadaWeekItemDTO item : items) {
            Long shiftId = item.getShiftId();
            StandardWeeklyShiftDTO shiftDTO = shifts.computeIfAbsent(
                    shiftId,
                    k -> new StandardWeeklyShiftDTO(k, null, new LinkedHashMap<>())
            );

            Map<String, List<StandardWeeklyRowDTO>> days = shiftDTO.getDays();
            List<StandardWeeklyRowDTO> rows = item.getRows().stream()
                    .map(this::toStandardWeeklyRow)
                    .toList();
            String dayKey = item.getDayKey() == null ? null : item.getDayKey().trim().toLowerCase();
            if (dayKey == null || dayKey.isBlank()) {
                continue;
            }
            days.put(dayKey, rows);
        }

        return new StandardWeeklyResponseDTO(
                plantId,
                weekResponse.getWeekStartDate(),
                status,
                List.copyOf(shifts.values())
        );
    }

    @Override
    public List<CascadaSummaryDTO> getCascadaStandardSummaries(
            String status,
            Long plantId,
            LocalDate weekStartDate,
            Long recipientUserId
    ) {
        String effectiveStatus = (status == null || status.isBlank()) ? CascadaStatus.SENT.name() : status;
        CascadaStatus cascadaStatus;
        try {
            cascadaStatus = CascadaStatus.valueOf(effectiveStatus);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid status: " + effectiveStatus);
        }

        List<CascadaStandardWeek> weeks = weekRepository.findByStatus(cascadaStatus);

        if (plantId != null) {
            weeks = weeks.stream()
                    .filter(week -> week.getPlant().getPlantId().equals(plantId))
                    .toList();
        }
        if (weekStartDate != null) {
            WeekMetadataResolver.ResolvedWeekMetadata requestedWeekMetadata = WeekMetadataResolver.resolve(
                    weekStartDate,
                    null,
                    null,
                    null
            );
            LocalDate effectiveWeekStartDate = requestedWeekMetadata.getWeekStartDate();
            weeks = weeks.stream()
                    .filter(week -> week.getWeekStartDate().equals(effectiveWeekStartDate))
                    .toList();
        }

        if (recipientUserId != null) {
            List<CascadaRecipient> recipients = cascadaRecipientRepository
                    .findByRecipientUserIdAndCascadaType(recipientUserId, CascadaType.STANDARD);
            java.util.Set<String> allowedKeys = recipients.stream()
                    .map(r -> r.getPlant().getPlantId() + "|" + r.getWeekStartDate() + "|" + r.getShiftId())
                    .collect(java.util.stream.Collectors.toSet());

            weeks = weeks.stream()
                    .filter(week -> allowedKeys.contains(
                            week.getPlant().getPlantId() + "|" + week.getWeekStartDate() + "|" + week.getShiftId()
                    ))
                    .toList();
        }

        java.util.Map<String, List<CascadaStandardWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + week.getWeekStartDate()
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<CascadaStandardWeek> groupWeeks = entry.getValue();
            CascadaStandardWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(CascadaStandardWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            java.util.Set<String> shiftIds = groupWeeks.stream()
                    .map(CascadaStandardWeek::getShiftId)
                    .filter(java.util.Objects::nonNull)
                    .sorted(this::compareShiftIds)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            java.util.Set<String> dayKeys = new java.util.LinkedHashSet<>();
            for (CascadaStandardWeek week : groupWeeks) {
                List<CascadaStandardCell> cells = cellRepository.findByWeek(week);
                for (CascadaStandardCell cell : cells) {
                    if (cell.getDayKey() != null) {
                        dayKeys.add(cell.getDayKey());
                    }
                }
            }

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getCascadaStandardWeekId(),
                    stableCascadaId(latest.getPlant().getPlantId(), latest.getWeekStartDate()),
                    latest.getPlant().getPlantId(),
                    latest.getPlant().getPlantName(),
                    latest.getPlant().getCompany().getCompanyId(),
                    latest.getPlant().getCompany().getCompanyName(),
                    sentBy,
                    latest.getWeekStartDate(),
                    latest.getWeekStartDate(),
                    latest.getWeekEndDate() != null ? latest.getWeekEndDate() : latest.getWeekStartDate().plusDays(6),
                    latest.getWeekNumber() != null
                            ? latest.getWeekNumber()
                            : WeekMetadataResolver.resolve(latest.getWeekStartDate(), latest.getWeekStartDate(), null, null).getWeekNumber(),
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

    private CascadaRowDTO toRowDTO(CascadaStandardCell cell) {
        CascadaRowDTO row = new CascadaRowDTO();
        row.setManualStandardRowId(cell.getManualRow() == null ? null : cell.getManualRow().getManualStandardRowId());
        row.setDriverId(cell.getDriver() == null ? null : cell.getDriver().getDriverId());
        row.setRouteId(cell.getRouteId());
        row.setE(cell.getE());
        row.setS(cell.getS());
        row.setEte(cell.getEte());
        row.setSte(cell.getSte());
        row.setDriverNameOverride(cell.getDriverNameOverride());
        return row;
    }

    private void enrichRow(
            CascadaRowDTO row,
            Driver driver,
            DriverPlantAssignment assignment,
            Route route
    ) {
        if (driver != null) {
            row.setDriverName(driver.getDriverName());
            row.setLastName(driver.getLastName());
        } else if (row.getDriverNameOverride() != null && !row.getDriverNameOverride().isBlank()) {
            row.setDriverName(row.getDriverNameOverride());
        }
        if (route != null) {
            row.setRouteName(route.getRouteName());
        } else if (assignment != null && assignment.getRoute() != null) {
            row.setRouteName(assignment.getRoute().getRouteName());
        }
    }

    private Map<Long, Driver> loadDrivers(List<CascadaStandardCell> cells) {
        List<Long> driverIds = cells.stream()
                .map(cell -> cell.getDriver().getDriverId())
                .distinct()
                .toList();
        return driverRepository.findAllById(driverIds).stream()
                .collect(Collectors.toMap(Driver::getDriverId, d -> d));
    }

    private Map<Long, DriverPlantAssignment> loadAssignmentsByPlant(Long plantId) {
        return driverPlantAssignmentRepository.findByPlantPlantId(plantId).stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getDriver().getDriverId(),
                        assignment -> assignment,
                        (a, b) -> a
                ));
    }

    private Map<Long, Route> loadRoutes(List<CascadaStandardCell> cells) {
        List<Long> routeIds = cells.stream()
                .map(CascadaStandardCell::getRouteId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (routeIds.isEmpty()) {
            return Map.of();
        }
        return routeRepository.findAllById(routeIds).stream()
                .collect(Collectors.toMap(Route::getRouteId, r -> r));
    }

    private List<CascadaRowDTO> buildMergedRows(
            Long plantId,
            CascadaStandardWeek week,
            String shiftId,
            String dayKey,
            List<CascadaStandardManualRow> manualRows
    ) {
        if (dayKey == null || dayKey.isBlank()) {
            return List.of();
        }

        List<CascadaStandardCell> cells = week == null
                ? List.of()
                : cellRepository.findByWeekAndDayKey(week, dayKey.trim().toLowerCase());
        List<CascadaRowDTO> savedRows = cells.stream()
                .map(this::toRowDTO)
                .toList();
        List<CascadaRowDTO> baseRows = new ArrayList<>(buildBaseRows(plantId, shiftId));
        baseRows.addAll(buildManualRows(manualRows));
        if (baseRows.isEmpty()) {
            return new ArrayList<>(savedRows);
        }
        return mergeRows(baseRows, savedRows);
    }

    private List<CascadaRowDTO> buildBaseRows(Long plantId, String shiftId) {
        Long parsedShiftId = parseShiftId(shiftId);
        List<Driver> drivers = driverRepository.findByShiftsShiftId(parsedShiftId);
        Map<Long, DriverPlantAssignment> assignmentsByDriverId = loadAssignmentsByPlant(plantId);
        List<CascadaRowDTO> rows = new ArrayList<>();
        for (Driver driver : drivers) {
            DriverPlantAssignment assignment = assignmentsByDriverId.get(driver.getDriverId());
            CascadaRowDTO row = new CascadaRowDTO();
            row.setDriverId(driver.getDriverId());
            row.setDriverName(driver.getDriverName());
            row.setLastName(driver.getLastName());
            if (assignment != null && assignment.getRoute() != null) {
                row.setRouteId(assignment.getRoute().getRouteId());
                row.setRouteName(assignment.getRoute().getRouteName());
            }
            row.setDriverNameOverride("");
            row.setE("");
            row.setS("");
            row.setEte("");
            row.setSte("");
            rows.add(row);
        }
        return rows;
    }

    private List<CascadaRowDTO> buildManualRows(List<CascadaStandardManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return List.of();
        }
        return manualRows.stream()
                .map(this::toManualTableRow)
                .toList();
    }

    private CascadaRowDTO toManualTableRow(CascadaStandardManualRow manualRow) {
        CascadaRowDTO row = new CascadaRowDTO();
        row.setManualStandardRowId(manualRow.getManualStandardRowId());
        row.setDriverId(null);
        row.setDriverName(manualRow.getDriverNameOverride());
        row.setLastName(null);
        row.setRouteId(manualRow.getRouteId());
        row.setRouteName(manualRow.getRouteName());
        row.setDriverNameOverride(manualRow.getDriverNameOverride());
        row.setE("");
        row.setS("");
        row.setEte("");
        row.setSte("");
        return row;
    }

    private CascadaStandardManualRowDTO toManualRowResponse(CascadaStandardManualRow row) {
        CascadaStandardManualRowDTO dto = new CascadaStandardManualRowDTO();
        dto.setManualStandardRowId(row.getManualStandardRowId());
        dto.setDriverNameOverride(row.getDriverNameOverride());
        dto.setRouteId(row.getRouteId());
        dto.setRouteName(row.getRouteName());
        dto.setSortOrder(row.getSortOrder());
        return dto;
    }

    private List<CascadaRowDTO> mergeRows(List<CascadaRowDTO> baseRows, List<CascadaRowDTO> savedRows) {
        Map<String, CascadaRowDTO> baseByKey = baseRows.stream()
                .collect(Collectors.toMap(this::rowIdentityKey, row -> row, (a, b) -> a, LinkedHashMap::new));

        List<CascadaRowDTO> merged = new ArrayList<>(baseRows);
        for (CascadaRowDTO saved : savedRows) {
            CascadaRowDTO base = baseByKey.get(rowIdentityKey(saved));
            if (base == null) {
                merged.add(saved);
                continue;
            }
            mergeRow(base, saved);
        }
        return merged;
    }

    private void mergeRow(CascadaRowDTO base, CascadaRowDTO saved) {
        if (base.getManualStandardRowId() == null && saved.getManualStandardRowId() != null) {
            base.setManualStandardRowId(saved.getManualStandardRowId());
        }
        if (base.getDriverId() == null && saved.getDriverId() != null) {
            base.setDriverId(saved.getDriverId());
        }
        if (base.getDriverName() == null && saved.getDriverName() != null) {
            base.setDriverName(saved.getDriverName());
        }
        if (base.getLastName() == null && saved.getLastName() != null) {
            base.setLastName(saved.getLastName());
        }
        if (base.getRouteId() == null && saved.getRouteId() != null) {
            base.setRouteId(saved.getRouteId());
        }
        if (base.getRouteName() == null && saved.getRouteName() != null) {
            base.setRouteName(saved.getRouteName());
        }
        base.setDriverNameOverride(normalizeValue(saved.getDriverNameOverride()));
        base.setE(normalizeValue(saved.getE()));
        base.setS(normalizeValue(saved.getS()));
        base.setEte(normalizeValue(saved.getEte()));
        base.setSte(normalizeValue(saved.getSte()));
    }

    private List<CascadaStandardManualRowDTO> toManualRowDTOs(List<CascadaStandardManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return List.of();
        }
        return manualRows.stream().map(this::toManualRowResponse).toList();
    }

    private Map<String, List<CascadaRowDTO>> normalizeDays(CascadaSaveRequestDTO request, boolean usesNewFlow) {
        Map<String, List<CascadaRowDTO>> normalizedDays = new LinkedHashMap<>();
        if (usesNewFlow) {
            String dayKey = request.getDayKey() == null ? null : request.getDayKey().trim().toLowerCase();
            if (dayKey == null || dayKey.isBlank()) {
                throw new RuntimeException("dayKey is required when rows is used");
            }
            normalizedDays.put(dayKey, new ArrayList<>(request.getRows()));
            return normalizedDays;
        }
        if (request.getDays() == null || request.getDays().isEmpty()) {
            return normalizedDays;
        }
        for (var entry : request.getDays().entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String normalizedKey = entry.getKey().trim().toLowerCase();
            if (normalizedKey.isBlank()) {
                continue;
            }
            List<CascadaRowDTO> rows = entry.getValue() == null ? List.of() : entry.getValue();
            normalizedDays.computeIfAbsent(normalizedKey, k -> new ArrayList<>()).addAll(rows);
        }
        return normalizedDays;
    }

    private List<CascadaStandardManualRow> saveManualRows(
            CascadaSaveRequestDTO request,
            Plant plant,
            LocalDateTime now,
            boolean usesNewFlow
    ) {
        List<CascadaStandardManualRow> existing = new ArrayList<>(
                manualRowRepository.findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(
                        request.getPlantId(),
                        request.getWeekDate()
                )
        );
        if (!usesNewFlow && request.getManualRows() == null) {
            return existing;
        }
        List<CascadaStandardManualRowDTO> requestedManualRows = collectRequestedManualRows(request);
        if (requestedManualRows.isEmpty()) {
            deleteManualRowsAndCells(existing);
            return List.of();
        }

        Map<Long, CascadaStandardManualRow> existingById = existing.stream()
                .filter(row -> row.getManualStandardRowId() != null)
                .collect(Collectors.toMap(CascadaStandardManualRow::getManualStandardRowId, row -> row, (a, b) -> a, LinkedHashMap::new));
        Map<String, CascadaStandardManualRow> existingByLogicalKey = existing.stream()
                .collect(Collectors.toMap(
                        this::manualRowLogicalKey,
                        row -> row,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        LinkedHashMap<String, CascadaStandardManualRowDTO> dedupedRequestedManualRows = new LinkedHashMap<>();
        for (CascadaStandardManualRowDTO dto : requestedManualRows) {
            CascadaStandardManualRow matchedExisting = resolveExistingManualRow(dto, existingById, existingByLogicalKey);
            String key = matchedExisting != null && matchedExisting.getManualStandardRowId() != null
                    ? "M:" + matchedExisting.getManualStandardRowId()
                    : "NEW:" + manualRowLogicalKey(dto);
            CascadaStandardManualRowDTO normalized = new CascadaStandardManualRowDTO();
            normalized.setManualStandardRowId(
                    dto.getManualStandardRowId() != null
                            ? dto.getManualStandardRowId()
                            : matchedExisting == null ? null : matchedExisting.getManualStandardRowId()
            );
            normalized.setDriverNameOverride(dto.getDriverNameOverride());
            normalized.setRouteId(dto.getRouteId());
            normalized.setRouteName(dto.getRouteName());
            normalized.setSortOrder(dto.getSortOrder());
            if (normalized.getManualStandardRowId() != null && matchedExisting != null) {
                existingById.putIfAbsent(normalized.getManualStandardRowId(), matchedExisting);
            }
            dedupedRequestedManualRows.put(key, normalized);
        }

        List<CascadaStandardManualRow> persisted = new ArrayList<>();
        int sequence = 0;
        for (CascadaStandardManualRowDTO dto : dedupedRequestedManualRows.values()) {
            CascadaStandardManualRow row = dto.getManualStandardRowId() == null
                    ? new CascadaStandardManualRow()
                    : existingById.getOrDefault(dto.getManualStandardRowId(), new CascadaStandardManualRow());
            row.setPlant(plant);
            row.setWeekDate(request.getWeekDate());
            row.setSortOrder(dto.getSortOrder() == null ? sequence : dto.getSortOrder());
            row.setDriverNameOverride(trimToNull(dto.getDriverNameOverride()));
            row.setRouteId(dto.getRouteId());
            row.setRouteName(trimToNull(dto.getRouteName()));
            row.setUpdatedAt(now);
            row.setUpdatedByUserId(null);
            persisted.add(manualRowRepository.save(row));
            sequence++;
        }

        List<CascadaStandardManualRow> toDelete = existing.stream()
                .filter(row -> persisted.stream().noneMatch(saved -> saved.getManualStandardRowId().equals(row.getManualStandardRowId())))
                .toList();
        deleteManualRowsAndCells(toDelete);

        return manualRowRepository.findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(
                request.getPlantId(),
                request.getWeekDate()
        );
    }

    private List<CascadaStandardManualRowDTO> collectRequestedManualRows(CascadaSaveRequestDTO request) {
        List<CascadaStandardManualRowDTO> requestedManualRows = new ArrayList<>();
        if (request.getManualRows() != null) {
            requestedManualRows.addAll(request.getManualRows());
        }

        if (request.getRows() != null) {
            for (CascadaRowDTO row : request.getRows()) {
                if (!isManualRow(row)) {
                    continue;
                }
                CascadaStandardManualRowDTO dto = new CascadaStandardManualRowDTO();
                dto.setManualStandardRowId(row.getManualStandardRowId());
                dto.setDriverNameOverride(row.getDriverNameOverride());
                dto.setRouteId(row.getRouteId());
                dto.setRouteName(row.getRouteName());
                requestedManualRows.add(dto);
            }
        }

        if (request.getDays() != null) {
            for (List<CascadaRowDTO> rows : request.getDays().values()) {
                if (rows == null) {
                    continue;
                }
                for (CascadaRowDTO row : rows) {
                    if (!isManualRow(row)) {
                        continue;
                    }
                    CascadaStandardManualRowDTO dto = new CascadaStandardManualRowDTO();
                    dto.setManualStandardRowId(row.getManualStandardRowId());
                    dto.setDriverNameOverride(row.getDriverNameOverride());
                    dto.setRouteId(row.getRouteId());
                    dto.setRouteName(row.getRouteName());
                    requestedManualRows.add(dto);
                }
            }
        }

        return requestedManualRows;
    }

    private CascadaStandardManualRow resolveExistingManualRow(
            CascadaStandardManualRowDTO dto,
            Map<Long, CascadaStandardManualRow> existingById,
            Map<String, CascadaStandardManualRow> existingByLogicalKey
    ) {
        if (dto == null) {
            return null;
        }
        if (dto.getManualStandardRowId() != null) {
            CascadaStandardManualRow existing = existingById.get(dto.getManualStandardRowId());
            if (existing != null) {
                return existing;
            }
            return manualRowRepository.findById(dto.getManualStandardRowId()).orElse(null);
        }
        return existingByLogicalKey.get(manualRowLogicalKey(dto));
    }

    private String manualRowLogicalKey(CascadaStandardManualRow row) {
        if (row == null) {
            return "NULL";
        }
        return manualRowLogicalKey(
                row.getDriverNameOverride(),
                row.getRouteId(),
                row.getRouteName()
        );
    }

    private String manualRowLogicalKey(CascadaStandardManualRowDTO row) {
        if (row == null) {
            return "NULL";
        }
        return manualRowLogicalKey(
                row.getDriverNameOverride(),
                row.getRouteId(),
                row.getRouteName()
        );
    }

    private String manualRowLogicalKey(String driverNameOverride, Long routeId, String routeName) {
        return "D:" + normalizeManualString(driverNameOverride)
                + "|RID:" + (routeId == null ? "NULL" : routeId)
                + "|RN:" + normalizeManualString(routeName);
    }

    private String normalizeManualString(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? "" : trimmed.toUpperCase();
    }

    private void deleteManualRowsAndCells(List<CascadaStandardManualRow> manualRows) {
        if (manualRows == null || manualRows.isEmpty()) {
            return;
        }
        List<Long> ids = manualRows.stream()
                .map(CascadaStandardManualRow::getManualStandardRowId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (!ids.isEmpty()) {
            List<CascadaStandardCell> cells = cellRepository.findByManualRowManualStandardRowIdIn(ids);
            if (!cells.isEmpty()) {
                cellRepository.deleteAll(cells);
            }
        }
        manualRowRepository.deleteAll(manualRows);
    }

    private CascadaStandardManualRow resolveManualRow(CascadaRowDTO row, List<CascadaStandardManualRow> manualRows) {
        if (row == null || row.getManualStandardRowId() == null) {
            return null;
        }
        if (manualRows != null && !manualRows.isEmpty()) {
            CascadaStandardManualRow persisted = manualRows.stream()
                    .filter(manualRow -> row.getManualStandardRowId().equals(manualRow.getManualStandardRowId()))
                    .findFirst()
                    .orElse(null);
            if (persisted != null) {
                return persisted;
            }
        }
        return manualRowRepository.findById(row.getManualStandardRowId()).orElse(null);
    }

    private boolean isManualRow(CascadaRowDTO row) {
        if (row == null) {
            return false;
        }
        if (row.getManualStandardRowId() != null) {
            return true;
        }
        return row.getDriverId() == null
                && row.getRouteId() == null
                && trimToNull(row.getDriverNameOverride()) != null;
    }

    private String rowIdentityKey(CascadaRowDTO row) {
        if (row.getManualStandardRowId() != null) {
            return "M:" + row.getManualStandardRowId();
        }
        return "D:" + row.getDriverId() + "|R:" + row.getRouteId();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value;
    }

    private Long parseShiftId(String shiftId) {
        if (shiftId == null) {
            return null;
        }
        try {
            return Long.parseLong(shiftId);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid shiftId stored: " + shiftId);
        }
    }

    private CascadaWeekTotalsDTO buildWeekTotals(Long plantId, List<CascadaWeekItemDTO> items) {
        Map<Long, Integer> byDriver = new HashMap<>();
        Map<Long, Integer> byRoute = new HashMap<>();
        Map<String, Integer> byDay = new LinkedHashMap<>();
        Map<String, Integer> byShift = new HashMap<>();
        Map<String, Integer> byDriverType = new HashMap<>();
        Map<String, Integer> byColumn = new LinkedHashMap<>();

        byColumn.put("E", 0);
        byColumn.put("S", 0);
        byColumn.put("ETE", 0);
        byColumn.put("STE", 0);

        for (String dayKey : List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom")) {
            byDay.put(dayKey, 0);
        }

        Map<Long, DriverType> driverTypes = driverPlantAssignmentRepository.findByPlantPlantId(plantId).stream()
                .collect(Collectors.toMap(
                        assignment -> assignment.getDriver().getDriverId(),
                        DriverPlantAssignment::getDriverType,
                        (a, b) -> a
                ));

        int weekTotal = 0;

        for (CascadaWeekItemDTO item : items) {
            String dayKey = item.getDayKey();
            String shiftKey = item.getShiftId() == null ? "UNKNOWN" : item.getShiftId().toString();
            for (CascadaRowDTO row : item.getRows()) {
                int rowCount = 0;
                rowCount += countColumn(row.getE(), "E", byColumn);
                rowCount += countColumn(row.getS(), "S", byColumn);
                rowCount += countColumn(row.getEte(), "ETE", byColumn);
                rowCount += countColumn(row.getSte(), "STE", byColumn);

                if (rowCount == 0) {
                    continue;
                }

                weekTotal += rowCount;
                addLongCount(byDriver, row.getDriverId(), rowCount);
                addLongCount(byRoute, row.getRouteId(), rowCount);
                addStringCount(byDay, dayKey, rowCount);
                addStringCount(byShift, shiftKey, rowCount);

                DriverType driverType = driverTypes.get(row.getDriverId());
                String driverTypeKey = driverType == null ? "UNKNOWN" : driverType.name();
                addStringCount(byDriverType, driverTypeKey, rowCount);
            }
        }

        return new CascadaWeekTotalsDTO(
                byDriver,
                byRoute,
                byDay,
                byShift,
                byDriverType,
                byColumn,
                weekTotal
        );
    }

    private int countColumn(
            String value,
            String columnKey,
            Map<String, Integer> byColumn
    ) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        addStringCount(byColumn, columnKey, 1);
        return 1;
    }

    private void addLongCount(Map<Long, Integer> totals, Long key, int count) {
        if (key == null || count == 0) {
            return;
        }
        totals.put(key, totals.getOrDefault(key, 0) + count);
    }

    private void addStringCount(Map<String, Integer> totals, String key, int count) {
        if (key == null || key.isBlank() || count == 0) {
            return;
        }
        totals.put(key, totals.getOrDefault(key, 0) + count);
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
            List<CascadaSummaryDTO> summaries = getCascadaStandardSummaries(
                    status,
                    plantId,
                    weekStartDate,
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

    private List<Long> recipientUserIdsFor(
            Long plantId,
            LocalDate weekStartDate,
            String shiftId,
            String dayKey
    ) {
        if (dayKey == null || dayKey.isBlank()) {
            return cascadaRecipientRepository.findRecipientUserIdsByShift(
                    plantId,
                    weekStartDate,
                    shiftId,
                    CascadaType.STANDARD
            );
        }
        return cascadaRecipientRepository.findRecipientUserIdsByDayKey(
                plantId,
                weekStartDate,
                shiftId,
                dayKey,
                CascadaType.STANDARD
        );
    }

    private String stableCascadaId(Long plantId, LocalDate weekStartDate) {
        return "standard-" + plantId + "-" + weekStartDate;
    }

    private java.util.Set<String> orderDayKeys(java.util.Set<String> dayKeys) {
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

    private StandardWeeklyRowDTO toStandardWeeklyRow(CascadaRowDTO row) {
        StandardWeeklyRowDTO dto = new StandardWeeklyRowDTO();
        dto.setManualStandardRowId(row.getManualStandardRowId());
        dto.setDriverId(row.getDriverId());
        dto.setDriverName(row.getDriverName());
        dto.setLastName(row.getLastName());
        dto.setDriverNameOverride(row.getDriverNameOverride());
        dto.setRouteId(row.getRouteId());
        dto.setRouteName(row.getRouteName());
        dto.setE(row.getE());
        dto.setS(row.getS());
        dto.setEte(row.getEte());
        dto.setSte(row.getSte());
        dto.setTotal(countRowTotal(row));
        return dto;
    }

    private int countRowTotal(CascadaRowDTO row) {
        int total = 0;
        if (isFilled(row.getE())) {
            total += 1;
        }
        if (isFilled(row.getS())) {
            total += 1;
        }
        if (isFilled(row.getEte())) {
            total += 1;
        }
        if (isFilled(row.getSte())) {
            total += 1;
        }
        return total;
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }
}
