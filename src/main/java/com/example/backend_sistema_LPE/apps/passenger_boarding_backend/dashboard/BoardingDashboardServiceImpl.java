package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByShiftResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardByUnitResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardDistributionResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardEventTypeMetricDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardKpisResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardRouteKpiDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardRouteMetricDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardShiftMetricDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardTimeSeriesPointDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardTimeSeriesResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardUnitKpiDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.dashboard.dto.BoardingDashboardUnitMetricDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventViewResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.BoardingEventQueryService;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BoardingDashboardServiceImpl implements BoardingDashboardService {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final Locale ES_MX = new Locale("es", "MX");
    private static final DateTimeFormatter DAY_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd MMM", ES_MX);
    private static final DateTimeFormatter HOUR_LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", ES_MX);
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", ES_MX);
    private static final WeekFields WEEK_FIELDS = WeekFields.of(DayOfWeek.MONDAY, 1);

    private final BoardingEventQueryService boardingEventQueryService;
    private final UnitRepository unitRepository;

    public BoardingDashboardServiceImpl(
            BoardingEventQueryService boardingEventQueryService,
            UnitRepository unitRepository
    ) {
        this.boardingEventQueryService = boardingEventQueryService;
        this.unitRepository = unitRepository;
    }

    @Override
    public BoardingDashboardKpisResponse getKpis(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    ) {
        DashboardContext context = buildDashboardContext(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        );

        long totalBoardings = context.rows().size();
        long activeUnits = context.rows().stream()
                .map(DashboardRow::unitId)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long incidents = context.rows().stream()
                .filter(this::isIncident)
                .count();
        double compliancePercentage = totalBoardings == 0
                ? 0.0
                : percentage(totalBoardings - incidents, totalBoardings);

        Map<Long, Long> boardingsByUnitId = countBy(context.rows(), DashboardRow::unitId);
        Map<String, Long> boardingsByShiftKey = countBy(
                context.rows().stream()
                        .filter(row -> row.shiftId() != null || !row.shiftName().isBlank())
                        .toList(),
                row -> row.shiftId() == null ? "shift-name:" + row.shiftName() : "shift-id:" + row.shiftId()
        );

        long daysInRange = Math.max(1, LocalDate.ofInstant(Instant.ofEpochSecond(from), ZONE_ID)
                .datesUntil(LocalDate.ofInstant(Instant.ofEpochSecond(to), ZONE_ID).plusDays(1))
                .count());

        double averagePerUnit = activeUnits == 0 ? 0.0 : ratio(totalBoardings, activeUnits);
        double averagePerShift = boardingsByShiftKey.isEmpty() ? 0.0 : ratio(totalBoardings, boardingsByShiftKey.size());
        double averagePerDay = ratio(totalBoardings, daysInRange);

        List<BoardingDashboardRouteMetricDTO> routes = buildRouteMetrics(context.rows());
        List<BoardingDashboardEventTypeMetricDTO> eventTypes = buildEventTypeMetrics(context.rows());

        return new BoardingDashboardKpisResponse(
                totalBoardings,
                activeUnits,
                incidents,
                round1(compliancePercentage),
                round1(averagePerUnit),
                round1(averagePerShift),
                round1(averagePerDay),
                resolveTopUnit(context.rows()),
                resolveLowestUnit(context.rows()),
                routes.isEmpty()
                        ? null
                        : new BoardingDashboardRouteKpiDTO(
                        routes.getFirst().routeCode(),
                        routes.getFirst().routeName(),
                        routes.getFirst().value()
                ),
                eventTypes.stream()
                        .filter(item -> "ENTRY".equals(item.type()))
                        .findFirst()
                        .map(BoardingDashboardEventTypeMetricDTO::percentage)
                        .orElse(0.0),
                eventTypes.stream()
                        .filter(item -> "EXIT".equals(item.type()))
                        .findFirst()
                        .map(BoardingDashboardEventTypeMetricDTO::percentage)
                        .orElse(0.0)
        );
    }

    @Override
    public BoardingDashboardByUnitResponse getBoardingsByUnit(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    ) {
        DashboardContext context = buildDashboardContext(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        );

        List<BoardingDashboardUnitMetricDTO> items = context.rows().stream()
                .collect(Collectors.groupingBy(
                        DashboardRow::unitId,
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    DashboardRow sample = context.rows().stream()
                            .filter(row -> Objects.equals(row.unitId(), entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    return new BoardingDashboardUnitMetricDTO(
                            entry.getKey(),
                            sample == null ? "" : sample.unitName(),
                            sample == null ? "" : sample.routeCode(),
                            entry.getValue()
                    );
                })
                .sorted(
                        Comparator.comparingLong(BoardingDashboardUnitMetricDTO::value).reversed()
                                .thenComparing(BoardingDashboardUnitMetricDTO::unitName, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();

        return new BoardingDashboardByUnitResponse(items);
    }

    @Override
    public BoardingDashboardByShiftResponse getBoardingsByShift(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    ) {
        DashboardContext context = buildDashboardContext(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        );

        long total = context.rows().size();
        List<BoardingDashboardShiftMetricDTO> items = context.rows().stream()
                .filter(row -> row.shiftId() != null || !row.shiftName().isBlank())
                .collect(Collectors.groupingBy(
                        row -> row.shiftId() == null ? "shift-name:" + row.shiftName() : "shift-id:" + row.shiftId(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    DashboardRow sample = context.rows().stream()
                            .filter(row -> {
                                String rowKey = row.shiftId() == null
                                        ? "shift-name:" + row.shiftName()
                                        : "shift-id:" + row.shiftId();
                                return rowKey.equals(entry.getKey());
                            })
                            .findFirst()
                            .orElse(null);
                    return new BoardingDashboardShiftMetricDTO(
                            sample == null ? null : sample.shiftId(),
                            sample == null ? "" : sample.shiftName(),
                            entry.getValue(),
                            total == 0 ? 0.0 : round1(percentage(entry.getValue(), total))
                    );
                })
                .sorted(
                        Comparator.comparingLong(BoardingDashboardShiftMetricDTO::value).reversed()
                                .thenComparing(BoardingDashboardShiftMetricDTO::shiftName, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();

        return new BoardingDashboardByShiftResponse(items);
    }

    @Override
    public BoardingDashboardTimeSeriesResponse getTimeSeries(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode,
            String groupBy
    ) {
        DashboardContext context = buildDashboardContext(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        );
        GroupByDimension dimension = parseGroupBy(groupBy);

        List<BoardingDashboardTimeSeriesPointDTO> items = switch (dimension) {
            case HOUR -> buildHourlySeries(context.rows());
            case DAY -> buildDailySeries(context.rows());
            case WEEK -> buildWeeklySeries(context.rows());
            case MONTH -> buildMonthlySeries(context.rows());
            case TIME_SLOT -> buildTimeSlotSeries(context.rows());
        };

        return new BoardingDashboardTimeSeriesResponse(dimension.name(), items);
    }

    @Override
    public BoardingDashboardDistributionResponse getDistribution(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    ) {
        DashboardContext context = buildDashboardContext(
                plantId,
                from,
                to,
                resolvedShiftId,
                windowType,
                unitId,
                routeCode
        );

        return new BoardingDashboardDistributionResponse(
                buildRouteMetrics(context.rows()),
                buildEventTypeMetrics(context.rows())
        );
    }

    private DashboardContext buildDashboardContext(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType,
            Long unitId,
            String routeCode
    ) {
        validateRequiredFilters(plantId, from, to);

        List<BoardingEventViewResponse> trips = boardingEventQueryService.findTrips(
                plantId,
                unitId,
                resolvedShiftId,
                windowType,
                from,
                to
        );

        Map<Long, Unit> unitsById = loadUnitsById(trips);
        String normalizedRouteCode = normalize(routeCode);

        List<DashboardRow> rows = trips.stream()
                .map(trip -> toDashboardRow(trip, unitsById.get(trip.getUnitId())))
                .filter(row -> normalizedRouteCode == null || normalizedRouteCode.equalsIgnoreCase(row.routeCode()))
                .toList();

        return new DashboardContext(rows);
    }

    private void validateRequiredFilters(Long plantId, Long from, Long to) {
        if (plantId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "plantId is required");
        }
        if (from == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from is required");
        }
        if (to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "to is required");
        }
        if (from > to) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from cannot be greater than to");
        }
    }

    private Map<Long, Unit> loadUnitsById(List<BoardingEventViewResponse> trips) {
        Set<Long> unitIds = trips.stream()
                .map(BoardingEventViewResponse::getUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (unitIds.isEmpty()) {
            return Map.of();
        }

        return unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(Unit::getUnitId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private DashboardRow toDashboardRow(BoardingEventViewResponse trip, Unit unit) {
        return new DashboardRow(
                trip.getBoardingEventId(),
                trip.getUnitId(),
                resolveUnitName(unit),
                defaultString(unit == null ? null : unit.getRouteCode()),
                defaultString(unit == null ? null : unit.getRouteName()),
                trip.getResolvedShiftId(),
                defaultString(trip.getShiftName()),
                defaultString(trip.getBoardingWindowType()),
                trip.getBoardingTime(),
                trip.getAlightingTime(),
                defaultString(trip.getStartLocationText()),
                defaultString(trip.getEndLocationText())
        );
    }

    private List<BoardingDashboardRouteMetricDTO> buildRouteMetrics(List<DashboardRow> rows) {
        long total = rows.size();
        return rows.stream()
                .filter(row -> !row.routeCode().isBlank() || !row.routeName().isBlank())
                .collect(Collectors.groupingBy(
                        row -> row.routeCode() + "|" + row.routeName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|", -1);
                    return new BoardingDashboardRouteMetricDTO(
                            parts.length > 0 ? parts[0] : "",
                            parts.length > 1 ? parts[1] : "",
                            entry.getValue(),
                            total == 0 ? 0.0 : round1(percentage(entry.getValue(), total))
                    );
                })
                .sorted(
                        Comparator.comparingLong(BoardingDashboardRouteMetricDTO::value).reversed()
                                .thenComparing(BoardingDashboardRouteMetricDTO::routeCode, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(BoardingDashboardRouteMetricDTO::routeName, String.CASE_INSENSITIVE_ORDER)
                )
                .toList();
    }

    private List<BoardingDashboardEventTypeMetricDTO> buildEventTypeMetrics(List<DashboardRow> rows) {
        long total = rows.size();
        Map<String, Long> counts = countBy(rows, row -> defaultString(row.eventType()).toUpperCase(Locale.ROOT));

        List<BoardingDashboardEventTypeMetricDTO> items = new ArrayList<>();
        items.add(new BoardingDashboardEventTypeMetricDTO(
                "ENTRY",
                "Entrada",
                counts.getOrDefault("ENTRY", 0L),
                total == 0 ? 0.0 : round1(percentage(counts.getOrDefault("ENTRY", 0L), total))
        ));
        items.add(new BoardingDashboardEventTypeMetricDTO(
                "EXIT",
                "Salida",
                counts.getOrDefault("EXIT", 0L),
                total == 0 ? 0.0 : round1(percentage(counts.getOrDefault("EXIT", 0L), total))
        ));
        return items;
    }

    private BoardingDashboardUnitKpiDTO resolveTopUnit(List<DashboardRow> rows) {
        return resolveUnitKpi(rows, Comparator.<Map.Entry<Long, Long>>comparingLong(Map.Entry::getValue)
                .reversed());
    }

    private BoardingDashboardUnitKpiDTO resolveLowestUnit(List<DashboardRow> rows) {
        return resolveUnitKpi(rows, Comparator.comparingLong(Map.Entry::getValue));
    }

    private BoardingDashboardUnitKpiDTO resolveUnitKpi(
            List<DashboardRow> rows,
            Comparator<Map.Entry<Long, Long>> primaryComparator
    ) {
        return countBy(rows, DashboardRow::unitId).entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .sorted(
                        primaryComparator.thenComparing(entry -> {
                            DashboardRow sample = findSampleByUnitId(rows, entry.getKey());
                            return sample == null ? "" : sample.unitName();
                        }, String.CASE_INSENSITIVE_ORDER)
                )
                .findFirst()
                .map(entry -> {
                    DashboardRow sample = findSampleByUnitId(rows, entry.getKey());
                    return new BoardingDashboardUnitKpiDTO(
                            entry.getKey(),
                            sample == null ? "" : sample.unitName(),
                            entry.getValue()
                    );
                })
                .orElse(null);
    }

    private DashboardRow findSampleByUnitId(List<DashboardRow> rows, Long unitId) {
        return rows.stream()
                .filter(row -> Objects.equals(row.unitId(), unitId))
                .findFirst()
                .orElse(null);
    }

    private boolean isIncident(DashboardRow row) {
        return row.alightingTime() == null || row.endLocationText().isBlank();
    }

    private List<BoardingDashboardTimeSeriesPointDTO> buildDailySeries(List<DashboardRow> rows) {
        Map<LocalDate, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> toLocalDateTime(row.boardingTime()).toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    long from = date.atStartOfDay(ZONE_ID).toEpochSecond();
                    long to = date.plusDays(1).atStartOfDay(ZONE_ID).toEpochSecond() - 1;
                    return new BoardingDashboardTimeSeriesPointDTO(
                            DAY_LABEL_FORMATTER.format(date),
                            entry.getValue(),
                            from,
                            to
                    );
                })
                .toList();
    }

    private List<BoardingDashboardTimeSeriesPointDTO> buildHourlySeries(List<DashboardRow> rows) {
        Map<LocalDateTime, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> toLocalDateTime(row.boardingTime())
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    LocalDateTime start = entry.getKey();
                    long from = start.atZone(ZONE_ID).toEpochSecond();
                    long to = start.plusHours(1).atZone(ZONE_ID).toEpochSecond() - 1;
                    return new BoardingDashboardTimeSeriesPointDTO(
                            HOUR_LABEL_FORMATTER.format(start),
                            entry.getValue(),
                            from,
                            to
                    );
                })
                .toList();
    }

    private List<BoardingDashboardTimeSeriesPointDTO> buildWeeklySeries(List<DashboardRow> rows) {
        Map<LocalDate, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> toLocalDateTime(row.boardingTime()).toLocalDate()
                                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    LocalDate weekEnd = weekStart.plusDays(6);
                    return new BoardingDashboardTimeSeriesPointDTO(
                            "Sem " + weekStart.get(WEEK_FIELDS.weekOfWeekBasedYear()) + " " + weekStart.getYear(),
                            entry.getValue(),
                            weekStart.atStartOfDay(ZONE_ID).toEpochSecond(),
                            weekEnd.plusDays(1).atStartOfDay(ZONE_ID).toEpochSecond() - 1
                    );
                })
                .toList();
    }

    private List<BoardingDashboardTimeSeriesPointDTO> buildMonthlySeries(List<DashboardRow> rows) {
        Map<LocalDate, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> toLocalDateTime(row.boardingTime()).toLocalDate().withDayOfMonth(1),
                        TreeMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    LocalDate monthStart = entry.getKey();
                    LocalDate nextMonth = monthStart.plusMonths(1);
                    return new BoardingDashboardTimeSeriesPointDTO(
                            MONTH_LABEL_FORMATTER.format(monthStart),
                            entry.getValue(),
                            monthStart.atStartOfDay(ZONE_ID).toEpochSecond(),
                            nextMonth.atStartOfDay(ZONE_ID).toEpochSecond() - 1
                    );
                })
                .toList();
    }

    private List<BoardingDashboardTimeSeriesPointDTO> buildTimeSlotSeries(List<DashboardRow> rows) {
        Map<Integer, Long> counts = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> (toLocalDateTime(row.boardingTime()).getHour() / 2) * 2,
                        TreeMap::new,
                        Collectors.counting()
                ));

        return counts.entrySet().stream()
                .map(entry -> {
                    int startHour = entry.getKey();
                    LocalTime from = LocalTime.of(startHour, 0);
                    LocalTime to = LocalTime.of(startHour, 0).plusHours(2).minusMinutes(1);
                    return new BoardingDashboardTimeSeriesPointDTO(
                            String.format("%02d:00 - %02d:59", startHour, (startHour + 1) % 24),
                            entry.getValue(),
                            null,
                            null
                    );
                })
                .toList();
    }

    private GroupByDimension parseGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return GroupByDimension.DAY;
        }
        try {
            return GroupByDimension.valueOf(groupBy.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid groupBy. Allowed values: HOUR, DAY, WEEK, MONTH, TIME_SLOT"
            );
        }
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp.toInstant().atZone(ZONE_ID).toLocalDateTime();
    }

    private <K> Map<K, Long> countBy(Collection<DashboardRow> rows, Function<DashboardRow, K> classifier) {
        return rows.stream()
                .collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()));
    }

    private double percentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return ((double) value * 100.0) / total;
    }

    private double ratio(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return (double) numerator / denominator;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private String resolveUnitName(Unit unit) {
        if (unit == null) {
            return "";
        }
        String internalId = defaultString(unit.getInternalId());
        if (!internalId.isBlank()) {
            return internalId;
        }
        return defaultString(unit.getNameRaw());
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record DashboardContext(
            List<DashboardRow> rows
    ) {
    }

    private record DashboardRow(
            Long boardingEventId,
            Long unitId,
            String unitName,
            String routeCode,
            String routeName,
            Long shiftId,
            String shiftName,
            String eventType,
            Timestamp boardingTime,
            Timestamp alightingTime,
            String startLocationText,
            String endLocationText
    ) {
    }

    private enum GroupByDimension {
        HOUR,
        DAY,
        WEEK,
        MONTH,
        TIME_SLOT
    }
}
