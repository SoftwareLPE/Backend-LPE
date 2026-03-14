package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatWeekCellDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTurnDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.model.FormatType;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.FormatTypeRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
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

    public FormatWeekPdfService(
            FormatWeekService formatWeekService,
            PlantRepository plantRepository,
            FormatTypeRepository formatTypeRepository,
            ShiftService shiftService
    ) {
        this.formatWeekService = formatWeekService;
        this.plantRepository = plantRepository;
        this.formatTypeRepository = formatTypeRepository;
        this.shiftService = shiftService;
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
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, plant, formatType, weekDate);

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

    private void addHeader(Document document, Plant plant, FormatType formatType, LocalDate weekDate) {
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, Color.BLACK);
        Font plantFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, Color.BLACK);
        Font metaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{38f, 62f});
        wrapper.setSpacingAfter(14f);

        PdfPCell leftCell = new PdfPCell(buildLeftHeaderTable(plant, formatType, smallFont, plantFont));
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(20f);

        PdfPCell rightCell = new PdfPCell(buildRightHeaderTable(plant, weekDate, metaFont));
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(20f);

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
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

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
        table.setWidths(buildColumnWidths(columns, baseColumns.size()));

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
                table.addCell(secondaryHeaderCell(config.getTurnName(), headerFont));
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
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setPaddingLeft(6f);
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
                    table.addCell(leftBodyCell(label, cellFont));
                } else {
                    table.addCell(bodyCell("", cellFont));
                }
            }
            return;
        }

        for (String column : baseColumns) {
            String value = resolveBaseColumnValue(column, row);
            boolean alignLeft = shouldAlignLeft(column);
            table.addCell(alignLeft ? leftBodyCell(value, cellFont) : bodyCell(value, cellFont));
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

        return new ArrayList<>(merged.values());
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

    private float[] buildColumnWidths(int columns, int baseColumns) {
        float baseWidth = columns <= 0 ? 1f : 100f / columns;
        float[] widths = new float[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = baseWidth;
        }
        for (int i = 0; i < baseColumns; i++) {
            widths[i] = i == 0 ? baseWidth * 1.45f : baseWidth * 1.6f;
        }
        widths[columns - 1] = baseWidth * 1.15f;
        return widths;
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
        cell.setPadding(5f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1f);
        cell.setPhrase(new Phrase(text == null ? "" : text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        return cell;
    }

    private PdfPCell secondaryHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(Color.WHITE);
        cell.setPadding(4f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(0.8f);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        cell.setBorderColor(new Color(190, 200, 210));
        cell.setBorderWidth(0.45f);
        return cell;
    }

    private PdfPCell leftBodyCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(6f);
        return cell;
    }

    private PdfPCell totalBodyCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK));
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

    private boolean shouldAlignLeft(String columnLabel) {
        if (columnLabel == null) {
            return false;
        }
        String normalized = columnLabel.trim().toLowerCase();
        return normalized.contains("ruta")
                || normalized.contains("recorrido")
                || normalized.contains("servicio")
                || normalized.contains("chofer")
                || normalized.contains("unidad");
    }

    private PdfPTable buildLeftHeaderTable(
            Plant plant,
            FormatType formatType,
            Font smallFont,
            Font plantFont
    ) {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell formatCell = new PdfPCell(new Phrase(formatDisplayName(formatType), smallFont));
        formatCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        formatCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        formatCell.setPadding(10f);
        formatCell.setBorderColor(Color.BLACK);
        formatCell.setBorderWidth(1.2f);
        table.addCell(formatCell);

        PdfPCell plantCell = new PdfPCell(new Phrase(safeValue(plant.getPlantName()).toUpperCase(Locale.ROOT), plantFont));
        plantCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        plantCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        plantCell.setPaddingTop(18f);
        plantCell.setPaddingBottom(18f);
        plantCell.setBorderColor(Color.BLACK);
        plantCell.setBorderWidth(1.2f);
        table.addCell(plantCell);

        return table;
    }

    private PdfPTable buildRightHeaderTable(Plant plant, LocalDate weekDate, Font metaFont) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{34f, 46f});
        } catch (Exception ignored) {
            // Static widths
        }

        table.addCell(metaLabelCell("SEMANA", metaFont));
        table.addCell(metaValueCell(formatWeekRange(weekDate), metaFont));
        table.addCell(metaLabelCell("COORDINADOR", metaFont));
        table.addCell(metaValueCell("", metaFont));
        return table;
    }

    private PdfPCell metaLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1.2f);
        return cell;
    }

    private PdfPCell metaValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBorderColor(Color.BLACK);
        cell.setBorderWidth(1.2f);
        return cell;
    }

    private String formatDisplayName(FormatType formatType) {
        if (formatType == null || formatType.getName() == null) {
            return "CUSTOM";
        }
        return formatType.getName().replace('_', ' ').toUpperCase(Locale.ROOT);
    }

    private String formatWeekRange(LocalDate weekDate) {
        if (weekDate == null) {
            return "";
        }
        LocalDate endDate = weekDate.plusDays(6);
        return formatSpanishDate(weekDate) + " AL " + formatSpanishDate(endDate);
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
}
