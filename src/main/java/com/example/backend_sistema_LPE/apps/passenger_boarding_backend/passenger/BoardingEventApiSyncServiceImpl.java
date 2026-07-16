package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventIngestionSummary;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger_group.PassengerGroup;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger_group.PassengerGroupRepository;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.ReportExecution;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitNameNormalizationService;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class BoardingEventApiSyncServiceImpl implements BoardingEventApiSyncService {
    private static final Logger log = LoggerFactory.getLogger(BoardingEventApiSyncServiceImpl.class);
    private static final Set<String> REQUIRED_HEADER_TYPES = Set.of("unit_name", "tag", "time_begin");

    private final PlantRepository plantRepository;
    private final PassengerGroupRepository passengerGroupRepository;
    private final UnitRepository unitRepository;
    private final PassengerRepository passengerRepository;
    private final BoardingEventRepository boardingEventRepository;
    private final ObjectMapper objectMapper;
    private final UnitNameNormalizationService unitNameNormalizationService;

    public BoardingEventApiSyncServiceImpl(
            PlantRepository plantRepository,
            PassengerGroupRepository passengerGroupRepository,
            UnitRepository unitRepository,
            PassengerRepository passengerRepository,
            BoardingEventRepository boardingEventRepository,
            ObjectMapper objectMapper,
            UnitNameNormalizationService unitNameNormalizationService
    ) {
        this.plantRepository = plantRepository;
        this.passengerGroupRepository = passengerGroupRepository;
        this.unitRepository = unitRepository;
        this.passengerRepository = passengerRepository;
        this.boardingEventRepository = boardingEventRepository;
        this.objectMapper = objectMapper;
        this.unitNameNormalizationService = unitNameNormalizationService;
    }

    @Override
    public BoardingEventIngestionSummary ingest(
            ReportExecution reportExecution,
            JsonNode execResponse,
            JsonNode rowsResponse,
            Long resourceId,
            Long objectSecId,
            int tableIndex
    ) {
        Plant plant = plantRepository.findByWialonId(resourceId).orElseThrow(() -> new IllegalStateException("Plant not found for wialon resourceId: " + resourceId));

        PassengerGroup passengerGroup = resolvePassengerGroup(plant, objectSecId);
        List<JsonNode> candidateRows = flattenRows(rowsResponse);
        Map<String, Integer> columnMap = resolveColumnMap(execResponse, tableIndex, resourceId);
        log.info("Column map construido para resourceId={}: {}", resourceId, columnMap);
        log.info("=== INICIO INGESTA === filas candidatas: {}", candidateRows.size());

        BoardingEventIngestionSummary result = new BoardingEventIngestionSummary();
        result.setCandidateRows(candidateRows.size());

        for (JsonNode row : candidateRows) {
            BoardingEvent event = parseBoardingEventRow(
                    row,
                    columnMap,
                    reportExecution,
                    plant,
                    passengerGroup,
                    resourceId,
                    objectSecId,
                    result
            );
            if (event == null) {
                continue;
            }

            boardingEventRepository.save(event);
            result.setInserted(result.getInserted() + 1);
            log.info("  guardada: boardingEvent para wialonRowKey={}", event.getWialonRowKey());
        }

        int skipped = result.getSkippedNoCellsArray()
                + result.getSkippedMissingRequiredColumns()
                + result.getSkippedMissingPassengerId()
                + result.getSkippedMissingUnitWialonId()
                + result.getSkippedDuplicateWialonRowKey();
        log.info("=== FIN INGESTA === guardados={}, descartados={}", result.getInserted(), skipped);

        return result;
    }

    private BoardingEvent parseBoardingEventRow(
            JsonNode row,
            Map<String, Integer> columnMap,
            ReportExecution reportExecution,
            Plant plant,
            PassengerGroup passengerGroup,
            Long resourceId,
            Long objectSecId,
            BoardingEventIngestionSummary result
    ) {
        JsonNode cells = row.path("c");
        logRowPreview(row, cells, columnMap);

        if (!cells.isArray()) {
            result.setSkippedNoCellsArray(result.getSkippedNoCellsArray() + 1);
            log.info("  descartada: cells no es array");
            return null;
        }
        if (!hasRequiredColumns(cells, columnMap)) {
            result.setSkippedMissingRequiredColumns(result.getSkippedMissingRequiredColumns() + 1);
            log.info("  descartada: columnas requeridas faltantes");
            return null;
        }

        String passengerExternalId = extractText(cells, columnMap, "tag");
        if (passengerExternalId == null || passengerExternalId.isBlank()) {
            result.setSkippedMissingPassengerId(result.getSkippedMissingPassengerId() + 1);
            log.info("  descartada: passengerExternalId vacío");
            return null;
        }

        long unitWialonId = readUnitWialonId(row, cells, columnMap);
        if (unitWialonId <= 0) {
            result.setSkippedMissingUnitWialonId(result.getSkippedMissingUnitWialonId() + 1);
            log.info("  descartada: unitWialonId inválido");
            return null;
        }

        String unitName = extractText(cells, columnMap, "unit_name");
        Unit unit = resolveUnit(plant, unitWialonId, unitName);
        Passenger passenger = resolvePassenger(passengerExternalId);
        Timestamp boardingTime = resolveBoardingTime(row, cells, columnMap);
        String rowNumber = readOptionalRowNumber(cells);

        String wialonRowKey = buildWialonRowKey(
                resourceId,
                objectSecId,
                unitWialonId,
                passengerExternalId,
                boardingTime
        );

        if (isDuplicateRowKey(wialonRowKey, result)) {
            return null;
        }

        return buildBoardingEvent(
                row,
                cells,
                columnMap,
                reportExecution,
                plant,
                passengerGroup,
                unit,
                passenger,
                passengerExternalId,
                boardingTime,
                rowNumber,
                wialonRowKey
        );
    }

    private void logRowPreview(JsonNode row, JsonNode cells, Map<String, Integer> columnMap) {
        int cellsSize = cells.isArray() ? cells.size() : 0;
        log.info("Fila tipo: {}, celdas: {}", row.path("t").asText(), cellsSize);
        log.info("  unit_name={} valor={}", columnMap.get("unit_name"), cellPreview(cells, columnMap.get("unit_name")));
        log.info("  tag={} valor={}", columnMap.get("tag"), cellPreview(cells, columnMap.get("tag")));
        log.info("  time_begin={} valor={}", columnMap.get("time_begin"), cellPreview(cells, columnMap.get("time_begin")));
    }

    private boolean isDuplicateRowKey(String wialonRowKey, BoardingEventIngestionSummary result) {
        log.info("  wialonRowKey: {}", wialonRowKey);
        boolean exists = boardingEventRepository.existsByWialonRowKey(wialonRowKey);
        log.info("  existe en BD: {}", exists);
        if (exists) {
            result.setSkippedDuplicateWialonRowKey(result.getSkippedDuplicateWialonRowKey() + 1);
            return true;
        }
        return false;
    }

    private BoardingEvent buildBoardingEvent(
            JsonNode row,
            JsonNode cells,
            Map<String, Integer> columnMap,
            ReportExecution reportExecution,
            Plant plant,
            PassengerGroup passengerGroup,
            Unit unit,
            Passenger passenger,
            String passengerExternalId,
            Timestamp boardingTime,
            String rowNumber,
            String wialonRowKey
    ) {
        BoardingEvent event = new BoardingEvent();
        event.setReportExecution(reportExecution);
        event.setPlant(plant);
        event.setPassengerGroup(passengerGroup);
        event.setUnit(unit);
        event.setPassenger(passenger);
        event.setRowNumber(rowNumber);
        event.setShift(extractText(cells, columnMap, "user_column"));
        event.setBoardingTime(boardingTime);
        event.setAlightingTime(resolveAlightingTime());
        event.setFinalTime(resolveFinalTime(cells, columnMap));
        event.setDuration(resolveDuration(cells, columnMap));

        JsonNode startLocation = findCell(cells, columnMap, "location_begin");
        event.setStartLocationText(readCellText(startLocation));
        event.setStartLatitude(resolveStartLatitude(cells, startLocation, columnMap));
        event.setStartLongitude(resolveStartLongitude(cells, startLocation, columnMap));

        JsonNode endLocation = findCell(cells, columnMap, "location_end");
        event.setEndLocationText(readCellText(endLocation));
        event.setEndLatitude(readCellDouble(endLocation, "y"));
        event.setEndLongitude(readCellDouble(endLocation, "x"));

        event.setWialonTagId(passengerExternalId);
        event.setWialonRowKey(wialonRowKey);
        event.setRawRowJson(toJson(row));
        return event;
    }

    private PassengerGroup resolvePassengerGroup(Plant plant, Long objectSecId) {
        Optional<PassengerGroup> existing = passengerGroupRepository.findByPlantPlantIdAndWialonId(
                plant.getPlantId(),
                objectSecId
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        PassengerGroup group = new PassengerGroup();
        group.setPlant(plant);
        group.setWialonId(objectSecId);
        group.setName("Wialon Group " + objectSecId);
        group.setDefault(false);
        group.setActive(true);
        group.setLastSyncedAt(Timestamp.from(Instant.now()));
        return passengerGroupRepository.save(group);
    }

    private Unit resolveUnit(Plant plant, long unitWialonId, String unitNameRaw) {
        Optional<Unit> existing = unitRepository.findByPlantPlantIdAndWialonId(plant.getPlantId(), unitWialonId);
        if (existing.isPresent()) {
            Unit unit = existing.get();
            if (unitNameRaw != null && !unitNameRaw.isBlank()) {
                unit.setNameRaw(unitNameRaw);
                unitNameNormalizationService.apply(unit, unitNameRaw);
            }
            unit.setLastSyncedAt(Timestamp.from(Instant.now()));
            unit.setActive(true);
            return unitRepository.save(unit);
        }

        Unit unit = new Unit();
        unit.setPlant(plant);
        unit.setWialonId(unitWialonId);
        unit.setNameRaw(unitNameRaw == null || unitNameRaw.isBlank() ? "UNIT-" + unitWialonId : unitNameRaw);
        unitNameNormalizationService.apply(unit, unit.getNameRaw());
        unit.setActive(true);
        unit.setLastSyncedAt(Timestamp.from(Instant.now()));
        return unitRepository.save(unit);
    }

    private Passenger resolvePassenger(String passengerExternalId) {
        return passengerRepository.findByWialonPassengerId(passengerExternalId)
                .orElseGet(() -> passengerRepository.save(new Passenger(
                        null,
                        passengerExternalId,
                        null,
                        null,
                        null,
                        null
                )));
    }

    private List<JsonNode> flattenRows(JsonNode rowsResponse) {
        List<JsonNode> rows = new ArrayList<>();
        if (rowsResponse == null || rowsResponse.isMissingNode() || rowsResponse.isNull()) {
            log.info("Filas aplanadas: 0 (rowsResponse vacío)");
            return rows;
        }

        JsonNode rootRows = rowsResponse.path("rows");
        if (rootRows.isArray()) {
            for (JsonNode row : rootRows) {
                collectRowRecursively(row, rows);
            }
            log.info("Filas aplanadas: {}", rows.size());
            return rows;
        }

        if (rowsResponse.isArray()) {
            for (JsonNode row : rowsResponse) {
                collectRowRecursively(row, rows);
            }
        }

        log.info("Filas aplanadas: {}", rows.size());
        return rows;
    }

    private void collectRowRecursively(JsonNode row, List<JsonNode> out) {
        if (row == null || row.isNull()) {
            return;
        }
        out.add(row);
        JsonNode nestedRows = row.path("r");
        if (nestedRows.isArray()) {
            for (JsonNode nested : nestedRows) {
                collectRowRecursively(nested, out);
            }
        }
    }

    private long readUnitWialonId(JsonNode row, JsonNode cells, Map<String, Integer> columnMap) {
        JsonNode uNode = row.path("u");
        if (uNode.isIntegralNumber()) {
            return uNode.asLong();
        }

        JsonNode startTimeCell = findCell(cells, columnMap, "time_begin");
        Long fromStartTimeCell = readCellLong(startTimeCell, "u");
        if (fromStartTimeCell != null && fromStartTimeCell > 0) {
            return fromStartTimeCell;
        }

        JsonNode startLocationCell = findCell(cells, columnMap, "location_begin");
        Long fromStartLocationCell = readCellLong(startLocationCell, "u");
        if (fromStartLocationCell != null && fromStartLocationCell > 0) {
            return fromStartLocationCell;
        }

        return 0L;
    }

    private Timestamp resolveBoardingTime(JsonNode row, JsonNode cells, Map<String, Integer> columnMap) {
        JsonNode startTimeCell = findCell(cells, columnMap, "time_begin");
        Long fromStartTimeCell = readCellUnixValue(startTimeCell);
        if (fromStartTimeCell != null && fromStartTimeCell > 0) {
            return Timestamp.from(Instant.ofEpochSecond(fromStartTimeCell));
        }

        // Fallback: t1 de fila
        long t1 = row.path("t1").asLong(0);
        if (t1 > 0) {
            return Timestamp.from(Instant.ofEpochSecond(t1));
        }

        Long fromC1 = readCellUnixValue(cells.size() > 1 ? cells.get(1) : null);
        if (fromC1 != null && fromC1 > 0) {
            return Timestamp.from(Instant.ofEpochSecond(fromC1));
        }

        return Timestamp.from(Instant.now());
    }

    private Timestamp resolveAlightingTime() {
        // In this template, Wialon does not provide a reliable passenger-level
        // alighting timestamp column. Keep null instead of deriving from t2.
        return null;
    }

    private String readCellText(JsonNode cell) {
        if (cell == null || cell.isNull()) {
            return null;
        }
        if (cell.isTextual()) {
            String text = cell.asText();
            return text == null || text.isBlank() ? null : text.trim();
        }
        if (cell.has("t")) {
            String text = cell.path("t").asText(null);
            return text == null || text.isBlank() ? null : text.trim();
        }
        if (cell.has("v")) {
            String value = cell.path("v").asText(null);
            return value == null || value.isBlank() ? null : value.trim();
        }
        return null;
    }

    private Long readCellUnixValue(JsonNode cell) {
        if (cell == null || cell.isNull()) {
            return null;
        }
        if (cell.isIntegralNumber()) {
            return cell.asLong();
        }
        if (cell.has("v") && cell.path("v").isIntegralNumber()) {
            return cell.path("v").asLong();
        }
        return null;
    }

    private Double readCellDouble(JsonNode cell, String field) {
        if (cell == null || cell.isNull()) {
            return null;
        }
        JsonNode value = cell.path(field);
        if (value.isNumber()) {
            return value.asDouble();
        }
        return null;
    }

    private Long readCellLong(JsonNode cell, String field) {
        if (cell == null || cell.isNull()) {
            return null;
        }
        JsonNode value = cell.path(field);
        if (value.isIntegralNumber()) {
            return value.asLong();
        }
        return null;
    }

    private Double resolveStartLatitude(JsonNode cells, JsonNode startLocationCell, Map<String, Integer> columnMap) {
        JsonNode startTimeCell = findCell(cells, columnMap, "time_begin");
        Double fromStartTimeCell = readCellDouble(startTimeCell, "y");
        if (fromStartTimeCell != null) {
            return fromStartTimeCell;
        }
        return readCellDouble(startLocationCell, "y");
    }

    private Double resolveStartLongitude(JsonNode cells, JsonNode startLocationCell, Map<String, Integer> columnMap) {
        JsonNode startTimeCell = findCell(cells, columnMap, "time_begin");
        Double fromStartTimeCell = readCellDouble(startTimeCell, "x");
        if (fromStartTimeCell != null) {
            return fromStartTimeCell;
        }
        return readCellDouble(startLocationCell, "x");
    }

    private boolean hasRequiredColumns(JsonNode cells, Map<String, Integer> columnMap) {
        return hasHeaderTypeCell(cells, columnMap, "unit_name")
                && hasHeaderTypeCell(cells, columnMap, "tag")
                && hasHeaderTypeCell(cells, columnMap, "time_begin");
    }

    private boolean hasHeaderTypeCell(JsonNode cells, Map<String, Integer> columnMap, String headerType) {
        Integer index = columnMap.get(headerType);
        return index != null && cells.isArray() && index >= 0 && index < cells.size();
    }

    private JsonNode safeCell(JsonNode cells, Integer index) {
        if (index == null || !cells.isArray() || index < 0 || index >= cells.size()) {
            return null;
        }
        return cells.get(index);
    }

    private JsonNode findCell(JsonNode cells, Map<String, Integer> columnMap, String headerType) {
        return safeCell(cells, columnMap.get(headerType));
    }

    private String cellPreview(JsonNode cells, Integer index) {
        JsonNode cell = safeCell(cells, index);
        return cell == null ? "FUERA DE RANGO" : cell.toString();
    }

    private String readOptionalRowNumber(JsonNode cells) {
        return safeCell(cells, 0) == null ? null : readCellText(cells.get(0));
    }

    private Map<String, Integer> resolveColumnMap(JsonNode execResponse, int tableIndex, Long resourceId) {
        JsonNode tableNode = readTableNode(execResponse, tableIndex);
        List<String> headerTypes = readHeaderTypes(tableNode);
        if (headerTypes.isEmpty()) {
            throw new IllegalStateException(
                    "Plantilla del recurso " + resourceId +
                            " no expone header_type en exec_report. Revisar configuracion de Wialon."
            );
        }
        Map<String, Integer> columnMap = buildColumnMap(headerTypes);
        if (!columnMap.keySet().containsAll(REQUIRED_HEADER_TYPES)) {
            throw new IllegalStateException(
                    "Plantilla del recurso " + resourceId +
                            " no expone header_type para columnas requeridas. Encontradas: " + columnMap.keySet()
            );
        }
        return columnMap;
    }

    private JsonNode readTableNode(JsonNode execResponse, int tableIndex) {
        if (execResponse == null || execResponse.isNull()) {
            return null;
        }
        JsonNode tables = execResponse.path("reportResult").path("tables");
        if (!tables.isArray() || tables.isEmpty()) {
            return null;
        }
        int safeIndex = tableIndex >= 0 && tableIndex < tables.size() ? tableIndex : 0;
        return tables.get(safeIndex);
    }

    private List<String> readHeaderTypes(JsonNode tableNode) {
        List<String> headerTypes = new ArrayList<>();
        if (tableNode == null || tableNode.isNull()) {
            return headerTypes;
        }
        JsonNode headerTypeNode = tableNode.path("header_type");
        if (!headerTypeNode.isArray()) {
            return headerTypes;
        }
        for (JsonNode node : headerTypeNode) {
            headerTypes.add(node == null || node.isNull() ? "" : node.asText(""));
        }
        return headerTypes;
    }

    private Map<String, Integer> buildColumnMap(List<String> headerTypes) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < headerTypes.size(); i++) {
            String type = headerTypes.get(i);
            if (type != null && !type.isBlank() && !map.containsKey(type)) {
                map.put(type, i);
            }
        }
        return map;
    }

    private Timestamp resolveFinalTime(JsonNode cells, Map<String, Integer> columnMap) {
        JsonNode finalTimeCell = findCell(cells, columnMap, "time_end");
        Long unix = readCellUnixValue(finalTimeCell);
        if (unix != null && unix > 0) {
            return Timestamp.from(Instant.ofEpochSecond(unix));
        }
        return null;
    }

    private String resolveDuration(JsonNode cells, Map<String, Integer> columnMap) {
        JsonNode durationCell = findCell(cells, columnMap, "duration");
        if (durationCell == null) {
            durationCell = findCell(cells, columnMap, "duration_ival");
        }
        if (durationCell == null || durationCell.isNull()) {
            return null;
        }
        String text = readCellText(durationCell);
        if (text != null && !text.isBlank()) {
            return text;
        }
        if (durationCell.has("v")) {
            String raw = durationCell.path("v").asText(null);
            return raw == null || raw.isBlank() ? null : raw.trim();
        }
        return null;
    }

    private String extractText(JsonNode cells, Map<String, Integer> columnMap, String headerType) {
        return readCellText(findCell(cells, columnMap, headerType));
    }

    private String buildWialonRowKey(
            Long resourceId,
            Long objectSecId,
            long unitWialonId,
            String passengerExternalId,
            Timestamp boardingTime
    ) {
        long boardingEpoch = boardingTime == null ? 0 : boardingTime.toInstant().getEpochSecond();
        return resourceId + "|" + objectSecId + "|" + unitWialonId + "|" + passengerExternalId + "|" + boardingEpoch;
    }

    private String toJson(JsonNode row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
