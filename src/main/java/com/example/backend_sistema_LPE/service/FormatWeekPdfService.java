package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatWeekCellDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekRowDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.FormatWeekTurnDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormatWeekPdfService {
    private final FormatWeekService formatWeekService;
    private final PlantRepository plantRepository;
    private final ShiftService shiftService;

    public FormatWeekPdfService(
            FormatWeekService formatWeekService,
            PlantRepository plantRepository,
            ShiftService shiftService
    ) {
        this.formatWeekService = formatWeekService;
        this.plantRepository = plantRepository;
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

        FormatWeekSchemaDTO schema = formatWeekService.getFormatWeekSchema(plant.getFormatTypeId());
        Map<String, List<FormatWeekTurnDTO>> turnConfigsByDay = schema.getDays() == null
                ? Map.of()
                : schema.getDays();

        List<ShiftDTO> shifts = shiftService.getShiftsByPlant(plantId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, plant);

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

    private void addHeader(Document document, Plant plant) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        String companyName = plant.getCompany() != null ? plant.getCompany().getCompanyName() : "";
        Paragraph mainTitle = new Paragraph("Cascada Custom", titleFont);
        Paragraph companyTitle = new Paragraph(companyName, titleFont);
        Paragraph plantTitle = new Paragraph(plant.getPlantName(), titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        companyTitle.setAlignment(Element.ALIGN_CENTER);
        plantTitle.setAlignment(Element.ALIGN_CENTER);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{70f, 30f});

        PdfPCell textCell = new PdfPCell();
        textCell.setBorder(PdfPCell.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        textCell.setVerticalAlignment(Element.ALIGN_TOP);
        textCell.setPadding(0f);
        textCell.addElement(mainTitle);
        textCell.addElement(companyTitle);
        textCell.addElement(plantTitle);

        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        logoCell.setVerticalAlignment(Element.ALIGN_TOP);
        logoCell.setPadding(0f);
        logoCell.setPaddingRight(0f);
        logoCell.setPaddingLeft(0f);

        try {
            ClassPathResource logoResource = new ClassPathResource("static/MARCA_OFICIAL_VERTICAL.png");
            if (logoResource.exists()) {
                Image logo = Image.getInstance(logoResource.getURL());
                logo.scaleToFit(150, 50);
                logo.setAlignment(Image.ALIGN_RIGHT);
                logoCell.addElement(logo);
            }
        } catch (Exception ignored) {
            // Logo optional
        }

        headerTable.addCell(textCell);
        headerTable.addCell(logoCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));
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
        int columns = baseColumns.size() + totalDayColumns;
        if (columns == 0) {
            return;
        }

        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setWidths(buildColumnWidths(columns, baseColumns.size()));

        for (String base : baseColumns) {
            PdfPCell header = headerCell(base, headerFont);
            header.setRowspan(2);
            table.addCell(header);
        }

        for (String dayKey : orderedDays) {
            int dayCols = turnConfigsByDay.getOrDefault(dayKey, List.of()).size();
            if (dayCols == 0) {
                continue;
            }
            PdfPCell dayHeader = headerCell(dayDisplayName(dayKey), headerFont);
            dayHeader.setColspan(dayCols);
            table.addCell(dayHeader);
        }

        for (String dayKey : orderedDays) {
            List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
            for (FormatWeekTurnDTO config : configs) {
                table.addCell(headerCell(config.getTurnName(), headerFont));
            }
        }

        for (FormatWeekRowDTO row : rows) {
            addBaseCells(table, row, baseColumns, cellFont);
            Map<Long, Integer> byTurnId = cellsByTurnId(row.getCells());
            for (String dayKey : orderedDays) {
                List<FormatWeekTurnDTO> configs = turnConfigsByDay.getOrDefault(dayKey, List.of());
                for (FormatWeekTurnDTO config : configs) {
                    Integer value = byTurnId.get(config.getTurnConfigId());
                    table.addCell(bodyCell(value == null ? "" : value.toString(), cellFont));
                }
            }
        }

        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void addBaseCells(
            PdfPTable table,
            FormatWeekRowDTO row,
            List<String> baseColumns,
            Font cellFont
    ) {
        if (row.getExtraRow() != null && row.getExtraRow()) {
            String label = safeValue(row.getSecondaryValue());
            table.addCell(bodyCell(label, cellFont));
            for (int i = 1; i < baseColumns.size(); i++) {
                table.addCell(bodyCell("", cellFont));
            }
            return;
        }

        for (String column : baseColumns) {
            table.addCell(bodyCell(resolveBaseColumnValue(column, row), cellFont));
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
                        (a, b) -> a,
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
            baseCell.setTripCount(cell.getTripCount());
        }
    }

    private float[] buildColumnWidths(int columns, int baseColumns) {
        float baseWidth = columns <= 0 ? 1f : 100f / columns;
        float[] widths = new float[columns];
        for (int i = 0; i < columns; i++) {
            widths[i] = baseWidth;
        }
        for (int i = 0; i < baseColumns; i++) {
            widths[i] = baseWidth * 1.2f;
        }
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

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(235, 240, 245));
        cell.setPadding(3f);
        cell.setBorderColor(new Color(220, 228, 236));
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(2f);
        cell.setBorderColor(new Color(220, 228, 236));
        cell.setBorderWidth(0.5f);
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

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
