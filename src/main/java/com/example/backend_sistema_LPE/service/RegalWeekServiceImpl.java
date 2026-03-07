package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalDetailDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSaveRequestDTO;
import com.example.backend_sistema_LPE.dto.CascadaSummaryDTO;
import com.example.backend_sistema_LPE.dto.InboxMessageDTO;
import com.example.backend_sistema_LPE.enums.CascadaStatus;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.DriverPlantAssignment;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.RegalDetail;
import com.example.backend_sistema_LPE.model.RegalTripType;
import com.example.backend_sistema_LPE.model.RegalWeek;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.DriverPlantAssignmentRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.RegalTripTypeRepository;
import com.example.backend_sistema_LPE.repository.RegalWeekRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegalWeekServiceImpl implements RegalWeekService {
    private final RegalWeekRepository regalWeekRepository;
    private final PlantRepository plantRepository;
    private final ShiftRepository shiftRepository;
    private final DriverRepository driverRepository;
    private final RegalTripTypeRepository regalTripTypeRepository;
    private final DriverPlantAssignmentRepository driverPlantAssignmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public RegalWeekServiceImpl(
            RegalWeekRepository regalWeekRepository,
            PlantRepository plantRepository,
            ShiftRepository shiftRepository,
            DriverRepository driverRepository,
            RegalTripTypeRepository regalTripTypeRepository,
            DriverPlantAssignmentRepository driverPlantAssignmentRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.regalWeekRepository = regalWeekRepository;
        this.plantRepository = plantRepository;
        this.shiftRepository = shiftRepository;
        this.driverRepository = driverRepository;
        this.regalTripTypeRepository = regalTripTypeRepository;
        this.driverPlantAssignmentRepository = driverPlantAssignmentRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public RegalWeekResponseDTO getRegalWeek(Long plantId, LocalDate weekDate, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        List<RegalWeek> weeks = shiftId == null
                ? regalWeekRepository.findByPlantPlantIdAndWeekDate(plantId, weekDate)
                : regalWeekRepository.findByPlantPlantIdAndWeekDateAndShiftShiftId(plantId, weekDate, shiftId);

        List<RegalWeekRowDTO> savedRows = weeks.stream()
                .map(this::toRowDTO)
                .toList();

        if (shiftId == null) {
            return new RegalWeekResponseDTO(plantId, weekDate, shiftId, savedRows);
        }

        List<RegalTripType> tripTypes = regalTripTypeRepository
                .findByPlantPlantIdAndActiveTrueOrderBySortOrderAsc(plantId);

        List<RegalWeekRowDTO> baseRows = buildBaseRows(plantId, shiftId, tripTypes);
        List<RegalWeekRowDTO> rows = mergeBaseRows(baseRows, savedRows);

        return new RegalWeekResponseDTO(plantId, weekDate, shiftId, rows);
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
        if (request.getShiftId() == null) {
            throw new RuntimeException("shiftId is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        regalWeekRepository.deleteByPlantPlantIdAndWeekDateAndShiftShiftId(
                request.getPlantId(),
                request.getWeekDate(),
                request.getShiftId()
        );

        List<RegalWeekRowDTO> rows = request.getRows() == null ? List.of() : request.getRows();
        for (RegalWeekRowDTO row : rows) {
            RegalWeek week = new RegalWeek();
            week.setPlant(plant);
            week.setShift(shift);
            week.setWeekDate(request.getWeekDate());
            week.setStatus(CascadaStatus.DRAFT);

            if (row.getDriverId() != null) {
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
    }

    @Override
    @Transactional
    public void updateRegalStatus(Long plantId, LocalDate weekDate, String status, Long userId) {
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

        if (targetStatus == CascadaStatus.SENT || targetStatus == CascadaStatus.DELETED) {
            publishRegalInboxMessagesForStatus(
                    targetStatus.name(),
                    plantId,
                    weekDate,
                    userId
            );
        }
    }

    @Override
    public List<CascadaSummaryDTO> getRegalSummaries(String status, Long plantId, LocalDate weekDate) {
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
            weeks = weeks.stream()
                    .filter(week -> week.getWeekDate().equals(weekDate))
                    .toList();
        }

        java.util.Map<String, List<RegalWeek>> grouped = weeks.stream()
                .collect(java.util.stream.Collectors.groupingBy(week ->
                        week.getPlant().getPlantId() + "|" + week.getWeekDate()
                ));

        List<CascadaSummaryDTO> summaries = new ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<RegalWeek> groupWeeks = entry.getValue();
            RegalWeek latest = groupWeeks.stream()
                    .filter(d -> d.getSentAt() != null)
                    .max(java.util.Comparator.comparing(RegalWeek::getSentAt))
                    .orElse(groupWeeks.get(0));

            java.util.Set<String> shiftIds = groupWeeks.stream()
                    .map(week -> week.getShift() == null ? null : week.getShift().getShiftId().toString())
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toSet());
            java.util.Set<String> dayKeys = groupWeeks.stream()
                    .flatMap(week -> week.getDetails().stream().map(RegalDetail::getDayOfWeek))
                    .collect(java.util.stream.Collectors.toSet());

            String sentBy = null;
            if (latest.getSentByUserId() != null) {
                User user = userRepository.findById(latest.getSentByUserId()).orElse(null);
                if (user != null) {
                    sentBy = user.getUserName();
                }
            }

            summaries.add(new CascadaSummaryDTO(
                    latest.getRegalWeekId(),
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
                driverId,
                week.getDriver() == null ? null : week.getDriver().getDriverName(),
                week.getDriver() == null ? null : week.getDriver().getLastName(),
                assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteId(),
                assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteName(),
                assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getLocation(),
                details
        );
    }

    private List<RegalWeekRowDTO> buildBaseRows(
            Long plantId,
            Long shiftId,
            List<RegalTripType> tripTypes
    ) {
        List<Driver> drivers = driverRepository.findByPlantPlantIdAndActiveTrue(plantId);
        List<RegalWeekRowDTO> rows = new ArrayList<>();

        for (Driver driver : drivers) {
            DriverPlantAssignment assignment = driverPlantAssignmentRepository
                    .findByDriverDriverIdAndPlantPlantId(driver.getDriverId(), plantId)
                    .orElse(null);

            List<RegalDetailDTO> details = buildEmptyDetails(tripTypes);

            rows.add(new RegalWeekRowDTO(
                    null,
                    driver.getDriverId(),
                    driver.getDriverName(),
                    driver.getLastName(),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteId(),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getRouteName(),
                    assignment == null || assignment.getRoute() == null ? null : assignment.getRoute().getLocation(),
                    details
            ));
        }

        return rows;
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
        java.util.Map<Long, RegalWeekRowDTO> baseByDriver = baseRows.stream()
                .filter(row -> row.getDriverId() != null)
                .collect(java.util.stream.Collectors.toMap(RegalWeekRowDTO::getDriverId, row -> row));

        List<RegalWeekRowDTO> merged = new ArrayList<>(baseRows);

        for (RegalWeekRowDTO saved : savedRows) {
            if (saved.getDriverId() == null) {
                merged.add(saved);
                continue;
            }
            RegalWeekRowDTO base = baseByDriver.get(saved.getDriverId());
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

    private void publishRegalInboxMessagesForStatus(
            String status,
            Long plantId,
            LocalDate weekDate,
            Long userId
    ) {
        if (userId == null) {
            return;
        }
        List<CascadaSummaryDTO> summaries = getRegalSummaries(status, plantId, weekDate);
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
