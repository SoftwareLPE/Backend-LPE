package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.BoardingEventQueryService;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventViewResponse;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.BoardingEventPdfExportRowDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.example.backend_sistema_LPE.apps.shared.shift.Shift;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BoardingEventPdfExportServiceImpl implements BoardingEventPdfExportService {

    private static final ZoneId REPORT_ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", new Locale("es", "MX"));

    private final BoardingEventQueryService boardingEventQueryService;
    private final PlantRepository plantRepository;
    private final UnitRepository unitRepository;
    private final ShiftRepository shiftRepository;

    public BoardingEventPdfExportServiceImpl(
            BoardingEventQueryService boardingEventQueryService,
            PlantRepository plantRepository,
            UnitRepository unitRepository,
            ShiftRepository shiftRepository
    ) {
        this.boardingEventQueryService = boardingEventQueryService;
        this.plantRepository = plantRepository;
        this.unitRepository = unitRepository;
        this.shiftRepository = shiftRepository;
    }

    @Override
    public byte[] exportPlantBoardingEventsPdf(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType
    ) {
        validateRequiredParameters(plantId, from, to);

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Plant not found with id: " + plantId
                ));

        List<BoardingEventViewResponse> boardingEvents = boardingEventQueryService.findTrips(
                plantId,
                null,
                resolvedShiftId,
                windowType,
                from,
                to
        );

        Map<Long, Unit> unitsById = loadUnitsById(boardingEvents);
        List<BoardingEventPdfExportRowDTO> rows = boardingEvents.stream()
                .map(event -> mapToPdfExportRow(event, unitsById.get(event.getUnitId())))
                .sorted(
                        Comparator.comparing(BoardingEventPdfExportRowDTO::unitName, String.CASE_INSENSITIVE_ORDER)
                                .thenComparing(BoardingEventPdfExportRowDTO::originDateTime, Comparator.nullsLast(Timestamp::compareTo))
                                .thenComparing(BoardingEventPdfExportRowDTO::wialonPassengerId, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                )
                .toList();

        String xhtml = buildReportXhtml(
                plant,
                rows,
                from,
                to,
                resolvedShiftId,
                windowType
        );

        return renderPdf(xhtml);
    }

    private void validateRequiredParameters(Long plantId, Long from, Long to) {
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

    private Map<Long, Unit> loadUnitsById(List<BoardingEventViewResponse> boardingEvents) {
        Set<Long> unitIds = boardingEvents.stream()
                .map(BoardingEventViewResponse::getUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (unitIds.isEmpty()) {
            return Map.of();
        }

        return unitRepository.findAllById(unitIds).stream()
                .collect(Collectors.toMap(Unit::getUnitId, unit -> unit, (left, right) -> left, LinkedHashMap::new));
    }

    private BoardingEventPdfExportRowDTO mapToPdfExportRow(BoardingEventViewResponse event, Unit unit) {
        return new BoardingEventPdfExportRowDTO(
                event.getUnitId(),
                resolveUnitName(unit),
                resolveRoute(unit),
                defaultString(event.getWialonPassengerId()),
                defaultString(event.getShiftName()),
                toEventTypeLabel(event.getBoardingWindowType()),
                event.getBoardingTime(),
                defaultString(event.getStartLocationText()),
                event.getAlightingTime(),
                resolveDestinationLabel(event)
        );
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

    private String resolveRoute(Unit unit) {
        if (unit == null) {
            return "";
        }

        String routeCode = defaultString(unit.getRouteCode());
        String routeName = defaultString(unit.getRouteName());

        if (!routeCode.isBlank() && !routeName.isBlank()) {
            return routeCode + " " + routeName;
        }
        if (!routeCode.isBlank()) {
            return routeCode;
        }
        return routeName;
    }

    private String resolveDestinationLabel(BoardingEventViewResponse event) {
        return defaultString(event.getEndLocationText());
    }

    private String toEventTypeLabel(String windowType) {
        if (windowType == null || windowType.isBlank()) {
            return "";
        }
        return switch (windowType.trim().toUpperCase(Locale.ROOT)) {
            case "ENTRY" -> "Entrada";
            case "EXIT" -> "Salida";
            default -> windowType;
        };
    }

    private String buildReportXhtml(
            Plant plant,
            List<BoardingEventPdfExportRowDTO> rows,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType
    ) {
        String generatedAt = formatInstant(Instant.now());
        String rangeLabel = formatEpochSeconds(from) + " - " + formatEpochSeconds(to);
        String shiftFilterLabel = resolveShiftFilterLabel(plant.getPlantId(), resolvedShiftId);
        String eventTypeFilterLabel = resolveEventTypeFilterLabel(windowType);
        long totalUnits = rows.stream()
                .map(BoardingEventPdfExportRowDTO::unitId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        StringBuilder html = new StringBuilder();
        html.append("""
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                    <style>
                      @page { size: A4 landscape; margin: 14mm 10mm; }
                      body { font-family: Arial, sans-serif; font-size: 9px; color: #111827; }
                      h1 { font-size: 18px; margin: 0 0 8px 0; }
                      h2 { font-size: 12px; margin: 18px 0 8px 0; }
                      .meta-table, .summary-table, .detail-table { width: 100%; border-collapse: collapse; }
                      .meta-table td { padding: 4px 6px; vertical-align: top; }
                      .summary-table td { padding: 6px; border: 1px solid #d1d5db; }
                      .detail-table th, .detail-table td {
                        border: 1px solid #cbd5e1;
                        padding: 5px;
                        vertical-align: top;
                      }
                      .detail-table th {
                        background: #e5e7eb;
                        font-weight: bold;
                        text-align: left;
                      }
                      .muted { color: #4b5563; }
                      .empty-state {
                        padding: 18px;
                        text-align: center;
                        border: 1px solid #d1d5db;
                        background: #f9fafb;
                      }
                    </style>
                  </head>
                  <body>
                """);

        html.append("<h1>Reporte general de abordajes</h1>");
        html.append("<table class=\"meta-table\">");
        html.append("<tr>")
                .append("<td><strong>Planta:</strong> ").append(escape(plant.getPlantName())).append("</td>")
                .append("<td><strong>Rango:</strong> ").append(escape(rangeLabel)).append("</td>")
                .append("<td><strong>Generado:</strong> ").append(escape(generatedAt)).append("</td>")
                .append("</tr>");
        html.append("<tr>")
                .append("<td><strong>Turno:</strong> ").append(escape(shiftFilterLabel)).append("</td>")
                .append("<td><strong>Tipo de evento:</strong> ").append(escape(eventTypeFilterLabel)).append("</td>")
                .append("<td><strong>Filtros:</strong> ").append(escape(buildFiltersSummary(shiftFilterLabel, eventTypeFilterLabel))).append("</td>")
                .append("</tr>");
        html.append("</table>");

        html.append("<h2>Resumen general</h2>");
        html.append("<table class=\"summary-table\">")
                .append("<tr>")
                .append("<td><strong>Total de unidades incluidas:</strong> ").append(totalUnits).append("</td>")
                .append("<td><strong>Total de registros:</strong> ").append(rows.size()).append("</td>")
                .append("</tr>")
                .append("</table>");

        html.append("<h2>Detalle completo</h2>");
        if (rows.isEmpty()) {
            html.append("<div class=\"empty-state\">No se encontraron abordajes para los filtros solicitados.</div>");
        } else {
            html.append("<table class=\"detail-table\">")
                    .append("<thead>")
                    .append("<tr>")
                    .append("<th>Unidad</th>")
                    .append("<th>Ruta</th>")
                    .append("<th>No. Reloj</th>")
                    .append("<th>Turno</th>")
                    .append("<th>Tipo de evento</th>")
                    .append("<th>Fecha/Hora Origen</th>")
                    .append("<th>Origen</th>")
                    .append("<th>Fecha/Hora Destino</th>")
                    .append("<th>Destino</th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");

            for (BoardingEventPdfExportRowDTO row : rows) {
                html.append("<tr>")
                        .append("<td>").append(escape(row.unitName())).append("</td>")
                        .append("<td>").append(escape(row.route())).append("</td>")
                        .append("<td>").append(escape(row.wialonPassengerId())).append("</td>")
                        .append("<td>").append(escape(row.shiftName())).append("</td>")
                        .append("<td>").append(escape(row.eventTypeLabel())).append("</td>")
                        .append("<td>").append(escape(formatTimestamp(row.originDateTime()))).append("</td>")
                        .append("<td>").append(escape(row.originLocation())).append("</td>")
                        .append("<td>").append(escape(formatTimestamp(row.destinationDateTime()))).append("</td>")
                        .append("<td>").append(escape(row.destinationLocation())).append("</td>")
                        .append("</tr>");
            }

            html.append("</tbody></table>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    private String resolveShiftFilterLabel(Long plantId, Long resolvedShiftId) {
        if (resolvedShiftId == null) {
            return "Todos";
        }

        return shiftRepository.findByShiftIdAndPlantPlantId(resolvedShiftId, plantId)
                .map(Shift::getShiftName)
                .orElse("Shift ID " + resolvedShiftId);
    }

    private String resolveEventTypeFilterLabel(String windowType) {
        if (windowType == null || windowType.isBlank()) {
            return "Todos";
        }
        return toEventTypeLabel(windowType);
    }

    private String buildFiltersSummary(String shiftFilterLabel, String eventTypeFilterLabel) {
        return "Turno: " + shiftFilterLabel + " | Tipo de evento: " + eventTypeFilterLabel;
    }

    private byte[] renderPdf(String xhtml) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo generar el PDF de abordajes", ex);
        }
    }

    private String formatEpochSeconds(Long epochSeconds) {
        return formatInstant(Instant.ofEpochSecond(epochSeconds));
    }

    private String formatInstant(Instant instant) {
        return DATE_TIME_FORMATTER.format(instant.atZone(REPORT_ZONE_ID));
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) {
            return "";
        }
        return DATE_TIME_FORMATTER.format(timestamp.toInstant().atZone(REPORT_ZONE_ID));
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value == null ? "" : value, "UTF-8");
    }
}
