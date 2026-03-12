package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.CreateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurDetailDTO;
import com.example.backend_sistema_LPE.dto.FlexsurManualRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.dto.UpdateFlexsurManualRowRequestDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.FlexsurDetail;
import com.example.backend_sistema_LPE.model.FlexsurManualRow;
import com.example.backend_sistema_LPE.model.FlexsurService;
import com.example.backend_sistema_LPE.model.FlexsurWeek;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.FlexsurManualRowRepository;
import com.example.backend_sistema_LPE.repository.FlexsurServiceRepository;
import com.example.backend_sistema_LPE.repository.FlexsurWeekRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
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

@Service
public class FlexsurWeekServiceImpl implements FlexsurWeekService {
    private final FlexsurWeekRepository flexsurWeekRepository;
    private final FlexsurManualRowRepository flexsurManualRowRepository;
    private final FlexsurServiceRepository flexsurServiceRepository;
    private final PlantRepository plantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public FlexsurWeekServiceImpl(
            FlexsurWeekRepository flexsurWeekRepository,
            FlexsurManualRowRepository flexsurManualRowRepository,
            FlexsurServiceRepository flexsurServiceRepository,
            PlantRepository plantRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.flexsurWeekRepository = flexsurWeekRepository;
        this.flexsurManualRowRepository = flexsurManualRowRepository;
        this.flexsurServiceRepository = flexsurServiceRepository;
        this.plantRepository = plantRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public FlexsurWeekResponseDTO getFlexsurWeek(Long plantId, LocalDate weekDate) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        List<FlexsurWeek> weeks = flexsurWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate);
        List<FlexsurManualRow> manualRows = flexsurManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualFlexsurRowIdAsc(plantId, weekDate);
        List<FlexsurService> services = flexsurServiceRepository
                .findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(plantId);

        List<FlexsurWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .toList();
        List<FlexsurWeekRowDTO> baseRows = new ArrayList<>(buildCatalogRows(services));
        baseRows.addAll(buildManualRows(manualRows));
        List<FlexsurWeekRowDTO> rows = mergeRows(baseRows, savedRows);

        FlexsurWeekTotalsDTO totals = buildTotals(rows);
        String status = resolveStatus(weeks);
        return new FlexsurWeekResponseDTO(plantId, weekDate, status, rows, totals);
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
        List<FlexsurManualRow> manualRows = flexsurManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualFlexsurRowIdAsc(
                        request.getPlantId(),
                        request.getWeekDate()
                );

        flexsurWeekRepository.deleteByPlantPlantIdAndWeekDate(
                request.getPlantId(),
                request.getWeekDate()
        );

        List<FlexsurWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        for (FlexsurWeekRowDTO row : rows) {
            FlexsurManualRow manualRow = resolveManualRow(row, manualRows);
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
                detail.setTrips(trips);
                detail.setExtraColumn(extraColumn);
                detail.setTotal(trips + extraColumn);
                detailEntities.add(detail);
            }
            week.setDetails(detailEntities);
            flexsurWeekRepository.save(week);
        }
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
        List<FlexsurManualRow> existing = flexsurManualRowRepository
                .findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualFlexsurRowIdAsc(
                        request.getPlantId(),
                        request.getWeekDate()
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
    public void updateFlexsurStatus(Long plantId, LocalDate weekDate, String status, Long userId) {
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

        List<FlexsurWeek> weeks = flexsurWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate);
        if (weeks.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
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

        if (targetStatus == CascadaStatus.SENT || targetStatus == CascadaStatus.DELETED) {
            publishFlexsurInboxMessagesForStatus(
                    targetStatus.name(),
                    plantId,
                    weekDate,
                    userId
            );
        }
    }

    @Override
    public List<CascadaSummaryDTO> getFlexsurSummaries(String status, Long plantId, LocalDate weekDate) {
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
            weeks = weeks.stream()
                    .filter(week -> week.getWeekDate().equals(weekDate))
                    .toList();
        }

        java.util.Map<String, List<FlexsurWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + week.getWeekDate()
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<FlexsurWeek> groupWeeks = entry.getValue();
            FlexsurWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(FlexsurWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            java.util.Set<String> shiftIds = java.util.Set.of();
            java.util.Set<String> dayKeys = groupWeeks.stream()
                    .flatMap(week -> week.getDetails().stream().map(detail -> mapDayKey(detail.getServiceDate())))
                    .collect(java.util.stream.Collectors.toSet());

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getFlexsurWeekId(),
                    latest.getPlant().getPlantId() + "-" + latest.getWeekDate(),
                    latest.getPlant().getPlantId(),
                    latest.getPlant().getPlantName(),
                    latest.getPlant().getCompany().getCompanyId(),
                    latest.getPlant().getCompany().getCompanyName(),
                    sentBy,
                    latest.getWeekDate(),
                    shiftIds,
                    dayKeys,
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
                details
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
                        List.of()
                ))
                .toList();
    }

    private List<FlexsurWeekRowDTO> buildCatalogRows(List<FlexsurService> services) {
        if (services == null || services.isEmpty()) {
            return List.of();
        }
        return services.stream()
                .map(service -> new FlexsurWeekRowDTO(
                        null,
                        null,
                        service.getServiceName(),
                        List.of()
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
                String dayKey = mapDayKey(detail.getServiceDate());
                int trips = detail.getTrips() == null ? 0 : detail.getTrips();
                int extra = detail.getExtraColumn() == null ? 0 : detail.getExtraColumn();
                int total = trips + extra;

                addStringCount(byDay, dayKey, total);
                addStringCount(byService, serviceName, total);
                addStringCount(byColumn, "trips", trips);
                addStringCount(byColumn, "extraColumn", extra);
                addStringCount(byColumn, "total", total);
                weekTotal += total;
            }
        }

        return new FlexsurWeekTotalsDTO(byDay, byService, byColumn, weekTotal);
    }

    private String resolveStatus(List<FlexsurWeek> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return null;
        }
        CascadaStatus status = weeks.get(0).getStatus();
        return status == null ? null : status.name();
    }

    private FlexsurManualRow resolveManualRow(FlexsurWeekRowDTO row, List<FlexsurManualRow> manualRows) {
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
        return flexsurManualRowRepository.findById(row.getManualFlexsurRowId()).orElse(null);
    }

    private boolean isManualRow(FlexsurWeekRowDTO row) {
        return row != null && row.getManualFlexsurRowId() != null;
    }

    private FlexsurManualRowDTO toManualRowDTO(FlexsurManualRow row) {
        return new FlexsurManualRowDTO(
                row.getManualFlexsurRowId(),
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
            Long userId
    ) {
        if (userId == null) {
            return;
        }

        List<CascadaSummaryDTO> summaries = getFlexsurSummaries(status, plantId, weekDate);
        if (summaries.isEmpty()) {
            return;
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

        messagingTemplate.convertAndSend("/topic/inbox/" + userId, payload);
    }
}
