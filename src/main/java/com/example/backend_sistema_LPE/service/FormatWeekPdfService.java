package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatWeekCellDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTurnDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.model.FormatWeek;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.FormatWeekRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormatWeekPdfService {
    private final FormatWeekService formatWeekService;
    private final PlantRepository plantRepository;
    private final FormatTypeRepository formatTypeRepository;
    private final ShiftService shiftService;
    private final FormatWeekRepository formatWeekRepository;
    private final UserRepository userRepository;

    public FormatWeekPdfService(
            FormatWeekService formatWeekService,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            ShiftService shiftService,
            FormatWeekRepository formatWeekRepository,
            UserRepository userRepository
    ) {
        this.formatWeekService = formatWeekService;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.shiftService = shiftService;
        this.formatWeekRepository = formatWeekRepository;
        this.userRepository = userRepository;
    }

    public byte[] buildCustomWeeklyPdf(Long plantId, LocalDate weekDate) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        if (plant.getFormatTypeId() == null) {
            throw new RuntimeException("formatTypeId is required for custom pdf");
        }
        FormatType formatType = formatTypeRepository.findById(plant.getFormatTypeId())
                .orElseThrow(() -> new RuntimeException("Format type not found"));

        FormatWeekSchemaDTO schema = formatWeekService.getFormatWeekSchema(plant.getFormatTypeId());
        Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay = schema.getDays() == null
                ? Map.of()
                : schema.getDays();

        List<ShiftDTO> shifts = shiftService.getShiftsByPlant(plantId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A3.rotate(), 10, 10, 12, 12);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        HeaderMetadata headerMetadata = resolveHeaderMetadata(plant, weekDate);
        addHeader(document, plant, headerMetadata);

        List<FormatWeekRowDTO> allRows = new ArrayList<>();
        for (ShiftDTO shift : shifts) {
            FormatWeekResponseDTO weekResponse = formatWeekService.getFormatWeek(
                    plantId,
                    plant.getFormatTypeId(),
                    weekDate,
                    shift.getShiftId()
            );
            if (weekResponse.getRows() != null) {
                allRows.addAll(weekResponse.getRows());
            }
        }
        List<FormatWeekRowDTO> mergedRows = mergeRows(allRows, schema.getBaseColumns());
        addWeeklyTable(document, schema, mergedRows, turnConfigsByDay);

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(Document document, Plant plant, HeaderMetadata headerMetadata) {
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Font.BOLD, Color.BLACK);
        Font plantFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLACK);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);

        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{42f, 58f});
        wrapper.setSpacingAfter(8f);

        PdfPCell leftCell = new PdfPCell(buildLeftHeaderTable(plant, companyFont, plantFont));
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(14f);
        leftCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

        PdfPCell rightCell = new PdfPCell(buildRightHeaderTable(headerMetadata, metaFont));
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(14f);
        rightCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

        wrapper.addCell(leftCell);
        wrapper.addCell(rightCell);
        document.add(wrapper);
    }

    private void addWeeklyTable(
            Document document,
            FormatWeekSchemaDTO schema,
            List<FormatWeekRowDTO> rows,
            Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay
    ) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 5);

        List<String> orderedDays = orderDays(null, turnConfigsByDay.keySet());
        List<String> baseColumns = schema.getBaseColumns() == null ? List.of() : schema.getBaseColumns();
        int totalDayColumns = orderedDays.stream()
                .mapToInt(day -> turnConfigsByDay.getOrDefault(day, List.of()).size())
                .sum();
        int columns = baseColumns.size() + totalDayColumns + 1;
        if (columns == 0) {
            return;
        }

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setWidths(buildColumnWidths(columns, baseColumns));

        for (String base : baseColumns) {
            PdfPCell header = primaryHeaderCell(base, headerFont);
            header.setRowspan(2);
            table.addCell(header);
        }

        for (String dayKey : orderedDays) {
            int dayCols = turnConfigsByDay.getOrDefault(dayKey, List.of()).size();
            if (dayCols == 0) {
                continue;
            }
            PdfPCell dayHeader = primaryHeaderCell(dayDisplayName(dayKey), headerFont);
            dayHeader.setColspan(dayCols);
            table.addCell(dayHeader);
        }

        PdfPCell totalHeader = primaryHeaderCell("TOTAL", headerFont);
        totalHeader.setRowspan(2);
        table.addCell(totalHeader);

        for (String dayKey : orderedDays) {
            List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
            for (FormatWeekTurnDTO config : configs) {
                table.addCell(secondaryHeaderCell(displayTurnHeader(config.getTurnName()), headerFont));
            }
        }

        for (FormatWeekRowDTO row : rows) {
            addBaseCells(table, row, baseColumns, cellFont);
            Map<Long, Integer> byTurnId = cellsByTurnId(row.getCells());
            int rowTotal = 0;
            for (String dayKey : orderedDays) {
                List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
                for (FormatWeekTurnDTO config : configs) {
                    Integer value = byTurnId.get(config.getTurnConfigId());
                    int tripCount = value == null ? 0 : value;
                    rowTotal += tripCount;
                    table.addCell(bodyCell(value == null || tripCount == 0 ? "" : value.toString(), cellFont));
                }
            }
            table.addCell(totalBodyCell(rowTotal == 0 ? "" : String.valueOf(rowTotal), cellFont));
        }

        addTotalsRows(table, rows, orderedDays, baseColumns, turnConfigsByDay, cellFont);

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addTotalsRows(
            PdfPTable table,
            List<FormatWeekRowDTO> rows,
            List<String> orderedDays,
            List<String> baseColumns,
            Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay,
            Font cellFont
    ) {
        Map<String, Map<Long, Integer>> byDayAndTurn = new LinkedHashMap<>();
        Map<String, Integer> byDay = new LinkedHashMap<>();
        int weekTotal = 0;

        for (String dayKey : orderedDays) {
            Map<Long, Integer> turnTotals = new LinkedHashMap<>();
            for (FormatWeekTurnDTO config : turnConfigsByDay.getOrDefault(dayKey, List.of())) {
                turnTotals.put(config.getTurnConfigId(), 0);
            }
            byDayAndTurn.put(dayKey, turnTotals);
            byDay.put(dayKey, 0);
        }

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

        addTotalByTurnRow(table, baseColumns, orderedDays, turnConfigsByDay, byDayAndTurn, weekTotal, cellFont);
        addTotalByDayRow(table, baseColumns, orderedDays, turnConfigsByDay, byDay, weekTotal, cellFont);
    }

    private void addTotalByTurnRow(
            PdfPTable table,
            List<String> baseColumns,
            List<String> orderedDays,
            Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay,
            Map<String, Map<Long, Integer>> byDayAndTurn,
            int weekTotal,
            Font cellFont
    ) {
        addTotalsLabelCells(table, baseColumns, "TOTAL POR TURNO", cellFont);
        for (String dayKey : orderedDays) {
            List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
            Map<Long, Integer> turnTotals = byDayAndTurn.getOrDefault(dayKey, Map.of());
            for (FormatWeekTurnDTO config : configs) {
                int total = turnTotals.getOrDefault(config.getTurnConfigId(), 0);
                table.addCell(totalBodyCell(total == 0 ? "" : String.valueOf(total), cellFont));
            }
        }
        table.addCell(totalBodyCell(weekTotal == 0 ? "" : String.valueOf(weekTotal), cellFont));
    }

    private void addTotalByDayRow(
            PdfPTable table,
            List<String> baseColumns,
            List<String> orderedDays,
            Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay,
            Map<String, Integer> byDay,
            int weekTotal,
            Font cellFont
    ) {
        addTotalsLabelCells(table, baseColumns, "TOTAL POR DIA", cellFont);
        for (String dayKey : orderedDays) {
            List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
            if (configs.isEmpty()) {
                continue;
            }
            int total = byDay.getOrDefault(dayKey, 0);
            PdfPCell dayTotalCell = totalBodyCell(total == 0 ? "" : String.valueOf(total), cellFont);
            dayTotalCell.setColspan(configs.size());
            table.addCell(dayTotalCell);
        }
        table.addCell(totalBodyCell(weekTotal == 0 ? "" : String.valueOf(weekTotal), cellFont));
    }

    private void addTotalsLabelCells(
            PdfPTable table,
            List<String> baseColumns,
            String label,
            Font cellFont
    ) {
        int mergedColumns = Math.max(baseColumns.size(), 1);
        PdfPCell labelCell = totalBodyCell(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK));
        labelCell.setColspan(mergedColumns);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(labelCell);
    }

    private void addBaseCells(
            PdfPTable table,
            FormatWeekRowDTO row,
            List<String> baseColumns,
            Font cellFont
    ) {
        if (row.getExtraRow() != null && row.getExtraRow()) {
            String label = safeValue(row.getSecondaryValue());
            int labelColumnIndex = baseColumns.size() > 1 ? 1 : 0;
            for (int i = 0; i < baseColumns.size(); i++) {
                if (i == labelColumnIndex) {
                    table.addCell(bodyCell(label, cellFont));
                } else {
                    table.addCell(bodyCell("", cellFont));
                }
            }
            return;
        }

        for (String column : baseColumns) {
            String value = resolveBaseColumnValue(column, row);
            table.addCell(bodyCell(value, cellFont));
        }
    }

    private Map<Long, Integer> cellsByTurnId(List<FormatWeekCellDTO> cells) {
        if (cells == null || cells.isEmpty()) {
            return Map.of();
        }
        return cells.stream()
                .filter(cell -> cell.getTurnConfigId() != null)
                .collect(Collectors.toMap(
                        FormatWeekCellDTO::getTurnConfigId,
                        cell -> cell.getTripCount() == null ? 0 : cell.getTripCount(),
                        Integer::sum,
                        LinkedHashMap::new
                ));
    }

    private List<FormatWeekRowDTO> mergeRows(List<FormatWeekRowDTO> rows, List<String> baseColumns) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }

        boolean usesDriver = baseColumns != null && baseColumns.stream()
                .anyMatch(label -> label != null && label.trim().toLowerCase().contains("chofer"));
        Map<String, FormatWeekRowDTO> merged = new LinkedHashMap<>();
        int fallbackIndex = 0;

        for (FormatWeekRowDTO row : rows) {
            String key = rowKey(row, usesDriver);
            if (key == null) {
                key = "ROW:" + (fallbackIndex++);
            }
            FormatWeekRowDTO existing = merged.get(key);
            if (existing == null) {
                merged.put(key, row);
                continue;
            }
            mergeRow(existing, row);
        }

        List<FormatWeekRowDTO> orderedRows = new ArrayList<>(merged.values());
        orderedRows.sort(buildRowComparator());
        return orderedRows;
    }

    private Comparator<FormatWeekRowDTO> buildRowComparator() {
        return Comparator
                .comparing(this::isExtraRow)
                .thenComparing(row -> row.getRouteId() == null ? Long.MAX_VALUE : row.getRouteId())
                .thenComparing(row -> safeSortableValue(row.getRouteName()))
                .thenComparing(row -> safeSortableValue(row.getSecondaryValue()))
                .thenComparing(row -> safeSortableValue(formatDriverName(row.getDriverName(), row.getDriverLastName())));
    }

    private boolean isExtraRow(FormatWeekRowDTO row) {
        return row != null && Boolean.TRUE.equals(row.getExtraRow());
    }

    private String safeSortableValue(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String rowKey(FormatWeekRowDTO row, boolean usesDriver) {
        if (row == null) {
            return null;
        }
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

    private void mergeRow(FormatWeekRowDTO base, FormatWeekRowDTO incoming) {
        if (base.getFormatWeekId() == null && incoming.getFormatWeekId() != null) {
            base.setFormatWeekId(incoming.getFormatWeekId());
        }
        if (base.getManualRowId() == null && incoming.getManualRowId() != null) {
            base.setManualRowId(incoming.getManualRowId());
        }
        if (base.getRouteName() == null && incoming.getRouteName() != null) {
            base.setRouteName(incoming.getRouteName());
        }
        if (base.getDriverName() == null && incoming.getDriverName() != null) {
            base.setDriverName(incoming.getDriverName());
        }
        if (base.getDriverLastName() == null && incoming.getDriverLastName() != null) {
            base.setDriverLastName(incoming.getDriverLastName());
        }
        if (base.getUnitType() == null && incoming.getUnitType() != null) {
            base.setUnitType(incoming.getUnitType());
        }
        if (base.getSecondaryValue() == null && incoming.getSecondaryValue() != null) {
            base.setSecondaryValue(incoming.getSecondaryValue());
        }

        Map<Long, FormatWeekCellDTO> baseCells = (base.getCells() == null ? List.<FormatWeekCellDTO>of() : base.getCells())
                .stream()
                .filter(cell -> cell.getTurnConfigId() != null)
                .collect(Collectors.toMap(FormatWeekCellDTO::getTurnConfigId, c -> c, (a, b) -> a));

        List<FormatWeekCellDTO> incomingCells = incoming.getCells() == null ? List.of() : incoming.getCells();
        for (FormatWeekCellDTO cell : incomingCells) {
            if (cell.getTurnConfigId() == null) {
                continue;
            }
            FormatWeekCellDTO baseCell = baseCells.get(cell.getTurnConfigId());
            if (baseCell == null) {
                if (base.getCells() == null) {
                    base.setCells(new ArrayList<>());
                }
                base.getCells().add(cell);
                continue;
            }
            int current = baseCell.getTripCount() == null ? 0 : baseCell.getTripCount();
            int incomingCount = cell.getTripCount() == null ? 0 : cell.getTripCount();
            baseCell.setTripCount(current + incomingCount);
        }
    }

    private float[] buildColumnWidths(int columns, List<String> baseColumns) {
        float[] widths = new float[columns];
        int baseColumnCount = baseColumns == null ? 0 : baseColumns.size();

        for (int i = 0; i < columns; i++) {
            widths[i] = 0.78f;
        }
        for (int i = 0; i < baseColumnCount; i++) {
            widths[i] = resolveBaseColumnWidth(baseColumns.get(i));
        }
        widths[columns - 1] = 1.1f;
        return widths;
    }

    private float resolveBaseColumnWidth(String columnLabel) {
        if (columnLabel == null) {
            return 1.6f;
        }
        String normalized = columnLabel.trim().toLowerCase();
        if (normalized.contains("ruta")) {
            return 1.6f;
        }
        if (normalized.contains("recorrido") || normalized.contains("servicio")) {
            return 2.9f;
        }
        if (normalized.contains("unidad")) {
            return 2.4f;
        }
        if (normalized.contains("chofer")) {
            return 2.7f;
        }
        return 1.8f;
    }

    private List<String> orderDays(java.util.Collection<String> shiftDayKeys, java.util.Collection<String> schemaDayKeys) {
        List<String> ordered = new ArrayList<>(List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom"));
        if (schemaDayKeys != null && !schemaDayKeys.isEmpty()) {
            ordered = ordered.stream().filter(schemaDayKeys::contains).collect(Collectors.toList());
        }
        if (shiftDayKeys != null && !shiftDayKeys.isEmpty()) {
            ordered = ordered.stream().filter(shiftDayKeys::contains).collect(Collectors.toList());
        }
        return ordered;
    }

    private String dayDisplayName(String dayKey) {
        return switch (dayKey) {
            case "lun" -> "Lunes";
            case "mar" -> "Martes";
            case "mie" -> "Miercoles";
            case "jue" -> "Jueves";
            case "vie" -> "Viernes";
            case "sab" -> "Sabado";
            case "dom" -> "Domingo";
            default -> dayKey;
        };
    }

    private PdfPCell primaryHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new Color(220, 0, 0));
        cell.setPadding(2.2f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1f);
        cell.setPhrase(new Phrase(text == null ? "" : text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, Color.WHITE)));
        return cell;
    }

    private PdfPCell secondaryHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(Color.WHITE);
        cell.setPadding(1.6f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.8f);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(1.6f);
        cell.setBorderColor(new Color(190, 200, 210));
        cell.setBorderWidth(0.45f);
        return cell;
    }

    private PdfPCell totalBodyCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 5, Color.BLACK));
        cell.setBackgroundColor(new Color(250, 250, 250));
        return cell;
    }

    private String formatDriverName(String name, String lastName) {
        String safeName = name == null ? "" : name.trim();
        String safeLast = lastName == null ? "" : lastName.trim();
        String full = (safeName + " " + safeLast).trim();
        return full.isBlank() ? safeLast : full;
    }

    private String resolveBaseColumnValue(String columnLabel, FormatWeekRowDTO row) {
        if (columnLabel == null) {
            return "";
        }
        String normalized = columnLabel.trim().toLowerCase();
        if (normalized.contains("chofer")) {
            return formatDriverName(row.getDriverName(), row.getDriverLastName());
        }
        if (normalized.contains("ruta")) {
            return safeValue(row.getRouteName());
        }
        if (normalized.contains("recorrido") || normalized.contains("servicio")) {
            return safeValue(row.getSecondaryValue());
        }
        if (normalized.contains("unidad")) {
            return safeValue(row.getUnitType());
        }
        return "";
    }

    private PdfPTable buildLeftHeaderTable(
            Plant plant,
            Font companyFont,
            Font plantFont
    ) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell companyCell = new PdfPCell(new Phrase(resolveCompanyName(plant), companyFont));
        companyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        companyCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        companyCell.setPaddingTop(5f);
        companyCell.setPaddingBottom(5f);
        companyCell.setBorderColor(Color.BLACK);
        companyCell.setBorderWidth(1.2f);
        table.addCell(companyCell);

        PdfPCell plantCell = new PdfPCell(new Phrase(safeValue(plant.getPlantName()).toUpperCase(Locale.ROOT), plantFont));
        plantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        plantCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        plantCell.setPaddingTop(10f);
        plantCell.setPaddingBottom(10f);
        plantCell.setBorderColor(Color.BLACK);
        plantCell.setBorderWidth(1.2f);
        table.addCell(plantCell);

        return table;
    }

    private PdfPTable buildRightHeaderTable(HeaderMetadata headerMetadata, Font metaFont) {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{30f, 54f, 16f});
        } catch (Exception ignored) {
            // Static widths
        }

        table.addCell(metaLabelCell("SEMANA", metaFont));
        table.addCell(metaValueCell(formatWeekRange(headerMetadata.getWeekStartDate(), headerMetadata.getWeekEndDate()), metaFont));
        PdfPCell weekNumberCell = new PdfPCell(new Phrase(String.valueOf(headerMetadata.getWeekNumber()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK)));
        weekNumberCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        weekNumberCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        weekNumberCell.setRowspan(2);
        weekNumberCell.setPaddingTop(6f);
        weekNumberCell.setPaddingBottom(6f);
        weekNumberCell.setBorderColor(Color.BLACK);
        weekNumberCell.setBorderWidth(1.2f);
        table.addCell(weekNumberCell);

        table.addCell(metaLabelCell("COORDINADOR", metaFont));
        table.addCell(metaValueCell(safeValue(headerMetadata.getCoordinatorName()), metaFont));
        return table;
    }

    private PdfPCell metaLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1.2f);
        return cell;
    }

    private PdfPCell metaValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1.2f);
        return cell;
    }

    private String displayTurnHeader(String turnName) {
        if (turnName == null) {
            return "";
        }
        String normalized = turnName.trim().toUpperCase(Locale.ROOT);
        if (normalized.startsWith("TE")) {
            return "TE";
        }
        return turnName;
    }

    private String resolveCompanyName(Plant plant) {
        if (plant == null || plant.getCompany() == null || plant.getCompany().getCompanyName() == null) {
            return "";
        }
        return plant.getCompany().getCompanyName().toUpperCase(Locale.ROOT);
    }

    private HeaderMetadata resolveHeaderMetadata(Plant plant, LocalDate weekDate) {
        if (plant == null || plant.getPlantId() == null || plant.getFormatTypeId() == null || weekDate == null) {
            WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(weekDate, null, null, null);
            return new HeaderMetadata(
                    weekMetadata.getWeekStartDate(),
                    weekMetadata.getWeekEndDate(),
                    weekMetadata.getWeekNumber(),
                    ""
            );
        }

        List<FormatWeek> weeks = formatWeekRepository.findByPlantPlantIdAndWeekDateAndFormatTypeFormatTypeId(
                plant.getPlantId(),
                weekDate,
                plant.getFormatTypeId()
        );
        if (weeks == null || weeks.isEmpty()) {
            WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(weekDate, null, null, null);
            return new HeaderMetadata(
                    weekMetadata.getWeekStartDate(),
                    weekMetadata.getWeekEndDate(),
                    weekMetadata.getWeekNumber(),
                    ""
            );
        }

        FormatWeek latest = weeks.stream()
                .filter(week -> week.getSentAt() != null)
                .max(Comparator.comparing(FormatWeek::getSentAt))
                .orElseGet(() -> weeks.stream()
                        .filter(week -> week.getUpdatedAt() != null)
                        .max(Comparator.comparing(FormatWeek::getUpdatedAt))
                        .orElse(weeks.get(0)));

        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                latest.getWeekDate(),
                latest.getWeekStartDate(),
                latest.getWeekEndDate(),
                latest.getWeekNumber()
        );

        String coordinatorName = "";
        Long sentById = latest.getSentByUserId() != null ? latest.getSentByUserId() : latest.getUpdatedByUserId();
        if (sentById != null) {
            User user = userRepository.findById(sentById).orElse(null);
            if (user != null) {
                String displayName = UserDisplayNameResolver.resolve(user);
                if (!displayName.isBlank()) {
                    coordinatorName = displayName.toUpperCase(Locale.ROOT);
                }
            }
        }

        return new HeaderMetadata(
                weekMetadata.getWeekStartDate(),
                weekMetadata.getWeekEndDate(),
                weekMetadata.getWeekNumber(),
                coordinatorName
        );
    }

    private String formatWeekRange(LocalDate weekStartDate, LocalDate weekEndDate) {
        if (weekStartDate == null || weekEndDate == null) {
            return "";
        }
        if (weekStartDate.getYear() == weekEndDate.getYear()
                && weekStartDate.getMonth() == weekEndDate.getMonth()) {
            String month = weekEndDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"))
                    .toUpperCase(Locale.ROOT);
            return String.format(
                    Locale.ROOT,
                    "%02d AL %02d DE %s %04d",
                    weekStartDate.getDayOfMonth(),
                    weekEndDate.getDayOfMonth(),
                    month,
                    weekEndDate.getYear()
            );
        }
        return formatSpanishDate(weekStartDate) + " AL " + formatSpanishDate(weekEndDate);
    }

    private String formatSpanishDate(LocalDate date) {
        if (date == null) {
            return "";
        }
        String month = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "MX"))
                .toUpperCase(Locale.ROOT);
        return String.format(
                Locale.ROOT,
                "%02d DE %s DE %04d",
                date.getDayOfMonth(),
                month,
                date.getYear()
        );
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private static final class HeaderMetadata {
        private final LocalDate weekStartDate;
        private final LocalDate weekEndDate;
        private final int weekNumber;
        private final String coordinatorName;

        private HeaderMetadata(
                LocalDate weekStartDate,
                LocalDate weekEndDate,
                int weekNumber,
                String coordinatorName
        ) {
            this.weekStartDate = weekStartDate;
            this.weekEndDate = weekEndDate;
            this.weekNumber = weekNumber;
            this.coordinatorName = coordinatorName;
        }

        private LocalDate getWeekStartDate() {
            return weekStartDate;
        }

        private LocalDate getWeekEndDate() {
            return weekEndDate;
        }

        private int getWeekNumber() {
            return weekNumber;
        }

        private String getCoordinatorName() {
            return coordinatorName;
        }
    }
}
