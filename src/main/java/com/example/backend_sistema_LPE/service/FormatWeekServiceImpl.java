package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.FormatExtraRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekCellDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTurnDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.CascadaRecipient;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.FormatWeek;
import com.example.backend_sistema_LPE.model.FormatWeekCell;
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
import com.example.backend_sistema_LPE.repository.CascadaRecipientRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RouteRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;
    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final CascadaRecipientRepository cascadaRecipientRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FormatWeekServiceImpl(
            FormatWeekRepository formatWeekRepository,
            FormatTypeRepository formatTypeRepository,
            FormatTurnConfigRepository formatTurnConfigRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository,
            RouteRepository routeRepository,
            DriverRepository driverRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            CascadaRecipientRepository cascadaRecipientRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.formatWeekRepository = formatWeekRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.formatTurnConfigRepository = formatTurnConfigRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
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

        List<FormatWeek> weeks = shiftId == null
                ? formatWeekRepository.findByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
                        plantId,
                        weekDate,
                        formatTypeId
                )
                : formatWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
                        plantId,
                        weekDate,
                        shiftId,
                        formatTypeId
                );

        List<FormatWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .toList();

        if (shiftId == null) {
            return new FormatWeekResponseDTO(plantId, weekDate, shiftId, formatTypeId, savedRows);
        }

        FormatType formatType = formatTypeRepository.findById(formatTypeId)
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        markExtraRows(formatType, savedRows);

        List<FormatTurnConfig> turnConfigs = formatTurnConfigRepository
                .findByFormatTypeFormatTypeId(formatTypeId);

        List<FormatWeekRowDTO> rows = new ArrayList<>(savedRows);

        if (!turnConfigs.isEmpty()) {
            List<FormatWeekRowDTO> baseRows = usesDriver(formatType)
                    ? buildDriverRows(plantId, shiftId, formatType, turnConfigs)
                    : buildRouteRows(plantId, formatType, turnConfigs);

            baseRows = new ArrayList<>(baseRows);
            baseRows.addAll(buildExtraRows(formatType, turnConfigs));

            if (!baseRows.isEmpty()) {
                rows = mergeBaseRows(baseRows, savedRows, usesDriver(formatType));
            }
        }

        return new FormatWeekResponseDTO(plantId, weekDate, shiftId, formatTypeId, rows);
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

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));
        FormatType formatType = formatTypeRepository.findById(request.getFormatTypeId())
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        Map<Long, FormatTurnConfig> turnConfigById = formatTurnConfigRepository
                .findByFormatTypeFormatTypeId(request.getFormatTypeId())
                .stream()
                .collect(Collectors.toMap(FormatTurnConfig::getTurnConfigId, c -> c));

        formatWeekRepository.deleteByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId(),
                request.getFormatTypeId()
        );

        List<FormatWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        for (FormatWeekRowDTO row : rows) {
            FormatWeek week = new FormatWeek();
            week.setPlant(plant);
            week.setShift(shift);
            week.setFormatType(formatType);
            week.setWeekDate(request.getWeekDate());
            week.setUnitType(row.getUnitType());
            week.setSecondaryValue(row.getSecondaryValue());
            week.setStatus(CascadaStatus.DRAFT);

            if (row.getRouteId() != null) {
                Route route = routeRepository.findById(row.getRouteId())
                        .orElseThrow(() -> new RuntimeException("Route not found: " + row.getRouteId()));
                week.setRoute(route);
            }
            if (row.getDriverId() != null) {
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
    public void updateFormatWeekStatus(
            Long plantId,
            Long formatTypeId,
            LocalDate weekDate,
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

        List<FormatWeek> weeks = formatWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftIdAndFormatTypeFormatTypeId(
                plantId,
                weekDate,
                shiftId,
                formatTypeId
        );

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
                    weekDate,
                    shiftId.toString(),
                    CascadaType.CUSTOM
            );

            Plant plant = plantRepository.findById(plantId)
                    .orElseThrow(() -> new RuntimeException("Plant not found"));

            List<CascadaRecipient> recipients = new java.util.ArrayList<>();
            for (Long recipientUserId : recipientUserIds) {
                CascadaRecipient recipient = new CascadaRecipient();
                recipient.setPlant(plant);
                recipient.setWeekStartDate(weekDate);
                recipient.setShiftId(shiftId.toString());
                recipient.setDayKey(dayKey);
                recipient.setCascadaType(CascadaType.CUSTOM);
                recipient.setRecipientUserId(recipientUserId);
                recipient.setSentAt(now);
                recipient.setSentByUserId(userId);
                recipients.add(recipient);
            }
            cascadaRecipientRepository.saveAll(recipients);
            publishFormatInboxMessagesForStatus(
                    CascadaStatus.SENT.name(),
                    plantId,
                    weekDate,
                    recipientUserIds
            );
            return;
        }

        if (cascadaStatus == CascadaStatus.DELETED) {
            publishFormatInboxMessagesForStatus(
                    CascadaStatus.DELETED.name(),
                    plantId,
                    weekDate,
                    recipientUserIdsForCustom(plantId, weekDate, shiftId.toString(), dayKey)
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

        List<FormatWeek> weeks;
        if (plantId != null && weekDate != null) {
            weeks = formatWeekRepository.findByStatusAndPlantPlantIdAndWeekDate(cascadaStatus, plantId, weekDate);
        } else if (plantId != null) {
            weeks = formatWeekRepository.findByStatusAndPlantPlantId(cascadaStatus, plantId);
        } else if (weekDate != null) {
            weeks = formatWeekRepository.findByStatusAndWeekDate(cascadaStatus, weekDate);
        } else {
            weeks = formatWeekRepository.findByStatus(cascadaStatus);
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
                                week.getPlant().getPlantId() + "|" + week.getWeekDate() + "|" + shiftKey
                        );
                    })
                    .toList();
        }

        Map<String, List<FormatWeek>> grouped = weeks.stream()
                .collect(Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + week.getWeekDate()
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

            Set<String> shiftIds = groupWeeks.stream()
                    .map(week -> week.getShift() == null ? null : week.getShift().getShiftId())
                    .filter(java.util.Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toSet());

            Set<String> dayKeys = new HashSet<>();
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
                    stableCascadaId(latest.getPlant().getPlantId(), latest.getWeekDate()),
                    latest.getPlant().getPlantId(),
                    latest.getPlant().getPlantName(),
                    latest.getPlant().getCompany().getCompanyId(),
                    latest.getPlant().getCompany().getCompanyName(),
                    sentBy,
                    latest.getWeekDate(),
                    shiftIds,
                    dayKeys,
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

        List<FormatExtraRowDTO> extraRows = extraRowLabels(formatType).stream()
                .map(FormatExtraRowDTO::new)
                .toList();

        return new FormatWeekSchemaDTO(formatTypeId, baseColumns, days, extraRows);
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

        return new FormatWeekRowDTO(
                week.getFormatWeekId(),
                week.getRoute() == null ? null : week.getRoute().getRouteId(),
                week.getRoute() == null ? null : week.getRoute().getRouteName(),
                week.getDriver() == null ? null : week.getDriver().getDriverId(),
                week.getDriver() == null ? null : week.getDriver().getDriverName(),
                week.getDriver() == null ? null : week.getDriver().getLastName(),
                week.getUnitType(),
                week.getSecondaryValue(),
                Boolean.FALSE,
                cells
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
                        route.getRouteId(),
                        route.getRouteName(),
                        null,
                        null,
                        null,
                        null,
                        formatType.getSecondaryColumn() == null ? null : route.getLocation(),
                        Boolean.FALSE,
                        buildEmptyCells(turnConfigs)
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
                    route == null ? null : route.getRouteId(),
                    route == null ? null : route.getRouteName(),
                    driver.getDriverId(),
                    driver.getDriverName(),
                    driver.getLastName(),
                    null,
                    formatType.getSecondaryColumn() == null ? null : route == null ? null : route.getLocation(),
                    Boolean.FALSE,
                    buildEmptyCells(turnConfigs)
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

    private List<FormatWeekRowDTO> buildExtraRows(
            FormatType formatType,
            List<FormatTurnConfig> turnConfigs
    ) {
        List<String> labels = extraRowLabels(formatType);
        if (labels.isEmpty()) {
            return List.of();
        }

        List<FormatWeekRowDTO> rows = new ArrayList<>();
        for (String label : labels) {
            rows.add(new FormatWeekRowDTO(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    label,
                    Boolean.TRUE,
                    buildEmptyCells(turnConfigs)
            ));
        }
        return rows;
    }

    private List<String> extraRowLabels(FormatType formatType) {
        if (formatType == null || formatType.getName() == null) {
            return List.of();
        }
        String name = formatType.getName().trim().toUpperCase();
        return switch (name) {
            case "BOSCH_JUP2" -> List.of("Viajes de voluntariado", "");
            case "TPI_PLANTA_MX1" -> List.of("Viajes de traslado", "Viajes de voluntariado");
            case "TPI_PLANTA_MX2_3" -> List.of("Viajes de voluntariado", "Viajes de traslado");
            default -> List.of();
        };
    }

    private void markExtraRows(FormatType formatType, List<FormatWeekRowDTO> rows) {
        List<String> labels = extraRowLabels(formatType);
        if (labels.isEmpty()) {
            return;
        }
        for (FormatWeekRowDTO row : rows) {
            if (row.getRouteId() != null || row.getDriverId() != null) {
                continue;
            }
            String label = row.getSecondaryValue() == null ? "" : row.getSecondaryValue();
            if (labels.contains(label)) {
                row.setExtraRow(Boolean.TRUE);
            }
        }
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
        if (base.getRouteName() == null && saved.getRouteName() != null) {
            base.setRouteName(saved.getRouteName());
        }
        if (base.getDriverName() == null && saved.getDriverName() != null) {
            base.setDriverName(saved.getDriverName());
        }
        if (base.getDriverLastName() == null && saved.getDriverLastName() != null) {
            base.setDriverLastName(saved.getDriverLastName());
        }
        if (base.getUnitType() == null && saved.getUnitType() != null) {
            base.setUnitType(saved.getUnitType());
        }
        if (base.getSecondaryValue() == null && saved.getSecondaryValue() != null) {
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

    private String stableCascadaId(Long plantId, LocalDate weekDate) {
        return plantId + "-" + weekDate;
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
}
