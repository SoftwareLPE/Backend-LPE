package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekItemDTO;
import com.example.backend_sistema_LPE.dto.CascadaWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.DriverViewDTO;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CascadaPdfService {
    private final CascadaService cascadaService;
    private final ShiftService shiftService;
    private final DriverService driverService;
    private final PlantRepository plantRepository;

    public CascadaPdfService(
            CascadaService cascadaService,
            ShiftService shiftService,
            DriverService driverService,
            PlantRepository plantRepository
    ) {
        this.cascadaService = cascadaService;
        this.shiftService = shiftService;
        this.driverService = driverService;
        this.plantRepository = plantRepository;
    }

    public byte[] buildWeeklyPdf(Long plantId, LocalDate weekDate, String status, Boolean activeDrivers) {
        CascadaWeekResponseDTO weekResponse = cascadaService.getWeekCascadas(plantId, weekDate, status);
        List<ShiftDTO> shifts = shiftService.getShiftsByPlant(plantId);
        List<DriverViewDTO> drivers = driverService.getDriversByPlant(plantId, activeDrivers);

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        Map<Long, Map<String, Map<Long, CascadaRowDTO>>> rowsByShift = buildRowsMap(weekResponse.getItems());
        Map<Long, String> driverNames = drivers.stream()
                .collect(Collectors.toMap(
                        DriverViewDTO::getDriverId,
                        d -> formatDriverName(d.getDriverName(), d.getLastName()),
                        (a, b) -> a
                ));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, plant);
        addShiftTables(document, shifts, drivers, rowsByShift);
        addSummaryTables(document, shifts, drivers, rowsByShift);

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(Document document, Plant plant) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        String companyName = plant.getCompany() != null ? plant.getCompany().getCompanyName() : "";
        Paragraph mainTitle = new Paragraph("Cascada Viajes", titleFont);
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
            // Logo optional; skip if not present.
        }

        headerTable.addCell(textCell);
        headerTable.addCell(logoCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));
    }

    private void addShiftTables(
            Document document,
            List<ShiftDTO> shifts,
            List<DriverViewDTO> drivers,
            Map<Long, Map<String, Map<Long, CascadaRowDTO>>> rowsByShift
    ) {
        Font shiftTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        for (ShiftDTO shift : shifts) {
            String shiftLabel = shift.getShiftName();
            if (shift.getStartTime() != null && shift.getEndTime() != null) {
                shiftLabel = shiftLabel + " (" + shift.getStartTime() + " - " + shift.getEndTime() + ")";
            }
            document.add(new Paragraph(shiftLabel, shiftTitleFont));
            document.add(new Paragraph(" "));

            List<String> orderedDays = orderDays(shift.getDayKeys());
            int columns = 1 + orderedDays.size() * 4 + 2;
            PdfPTable table = new PdfPTable(columns);
            table.setWidthPercentage(100);
            table.setWidths(buildShiftTableWidths(orderedDays.size(), 2));

            PdfPCell driverHeader = headerCell("Chofer", headerFont);
            driverHeader.setRowspan(3);
            table.addCell(driverHeader);

            for (String dayKey : orderedDays) {
                PdfPCell dayHeader = headerCell(dayDisplayName(dayKey), headerFont);
                dayHeader.setColspan(4);
                table.addCell(dayHeader);
            }
            PdfPCell totalNormalHeader = headerCell("Total Norm", headerFont);
            totalNormalHeader.setRowspan(3);
            table.addCell(totalNormalHeader);
            PdfPCell totalExtraHeader = headerCell("Total Ext", headerFont);
            totalExtraHeader.setRowspan(3);
            table.addCell(totalExtraHeader);

            for (int i = 0; i < orderedDays.size(); i++) {
                PdfPCell eHeader = headerCell("E", headerFont);
                eHeader.setRowspan(2);
                table.addCell(eHeader);
                PdfPCell sHeader = headerCell("S", headerFont);
                sHeader.setRowspan(2);
                table.addCell(sHeader);
                PdfPCell teHeader = headerCell("T.E.", headerFont);
                teHeader.setColspan(2);
                table.addCell(teHeader);
            }

            for (int i = 0; i < orderedDays.size(); i++) {
                table.addCell(headerCell("E", headerFont));
                table.addCell(headerCell("S", headerFont));
            }

            Map<String, int[]> totalsByDay = new LinkedHashMap<>();
            for (String dayKey : orderedDays) {
                totalsByDay.put(dayKey, new int[4]);
            }
            int totalNormalSum = 0;
            int totalExtraSum = 0;

            Map<String, Map<Long, CascadaRowDTO>> byDay = rowsByShift.getOrDefault(shift.getShiftId(), Map.of());

            for (DriverViewDTO driver : drivers) {
                table.addCell(driverCell(formatDriverName(driver.getDriverName(), driver.getLastName()), cellFont));
                int driverNormal = 0;
                int driverExtra = 0;
                for (String dayKey : orderedDays) {
                    CascadaRowDTO row = byDay.getOrDefault(dayKey, Map.of()).get(driver.getDriverId());
                    String e = row != null ? row.getE() : "";
                    String s = row != null ? row.getS() : "";
                    String ete = row != null ? row.getEte() : "";
                    String ste = row != null ? row.getSte() : "";
                    table.addCell(bodyCell(e, cellFont));
                    table.addCell(bodyCell(s, cellFont));
                    table.addCell(bodyCell(ete, cellFont));
                    table.addCell(bodyCell(ste, cellFont));
                    int[] totals = totalsByDay.get(dayKey);
                    if (isFilled(e)) {
                        totals[0] += 1;
                        driverNormal += 1;
                    }
                    if (isFilled(s)) {
                        totals[1] += 1;
                        driverNormal += 1;
                    }
                    if (isFilled(ete)) {
                        totals[2] += 1;
                        driverExtra += 1;
                    }
                    if (isFilled(ste)) {
                        totals[3] += 1;
                        driverExtra += 1;
                    }
                }
                totalNormalSum += driverNormal;
                totalExtraSum += driverExtra;
                table.addCell(bodyCell(String.valueOf(driverNormal), cellFont));
                table.addCell(bodyCell(String.valueOf(driverExtra), cellFont));
            }

            PdfPCell totalLabel = headerCell("Total", headerFont);
            table.addCell(totalLabel);
            for (String dayKey : orderedDays) {
                int[] totals = totalsByDay.get(dayKey);
                table.addCell(headerCell(String.valueOf(totals[0]), headerFont));
                table.addCell(headerCell(String.valueOf(totals[1]), headerFont));
                table.addCell(headerCell(String.valueOf(totals[2]), headerFont));
                table.addCell(headerCell(String.valueOf(totals[3]), headerFont));
            }
            table.addCell(headerCell(String.valueOf(totalNormalSum), headerFont));
            table.addCell(headerCell(String.valueOf(totalExtraSum), headerFont));

            document.add(table);
            document.add(new Paragraph(" "));
        }
    }

    private void addSummaryTables(
            Document document,
            List<ShiftDTO> shifts,
            List<DriverViewDTO> drivers,
            Map<Long, Map<String, Map<Long, CascadaRowDTO>>> rowsByShift
    ) {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        Map<Long, DriverTotals> driverTotals = new HashMap<>();
        Map<String, Integer> routeTotals = new HashMap<>();
        int totalNormal = 0;
        int totalExtra = 0;

        for (var shiftEntry : rowsByShift.entrySet()) {
            Map<String, Map<Long, CascadaRowDTO>> byDay = shiftEntry.getValue();
            for (var dayEntry : byDay.entrySet()) {
                for (CascadaRowDTO row : dayEntry.getValue().values()) {
                    DriverTotals totals = driverTotals.computeIfAbsent(row.getDriverId(), k -> new DriverTotals());
                    totalNormal += countNormal(row, totals);
                    totalExtra += countExtra(row, totals);

                    addRouteCount(routeTotals, row.getE());
                    addRouteCount(routeTotals, row.getS());
                    addRouteCount(routeTotals, row.getEte());
                    addRouteCount(routeTotals, row.getSte());
                }
            }
        }

        int totalViajes = totalNormal + totalExtra;

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{52f, 16f, 16f, 16f});
        summaryTable.addCell(headerCell("Nombre de chofer", headerFont));
        summaryTable.addCell(headerCell("Normal", headerFont));
        summaryTable.addCell(headerCell("Extra", headerFont));
        summaryTable.addCell(headerCell("Total", headerFont));

        List<DriverViewDTO> sortedDrivers = new ArrayList<>(drivers);
        sortedDrivers.sort(Comparator.comparing(d -> formatDriverName(d.getDriverName(), d.getLastName())));

        int normalSum = 0;
        int extraSum = 0;
        for (DriverViewDTO driver : sortedDrivers) {
            DriverTotals totals = driverTotals.getOrDefault(driver.getDriverId(), new DriverTotals());
            summaryTable.addCell(bodyCell(formatDriverName(driver.getDriverName(), driver.getLastName()), cellFont));
            summaryTable.addCell(bodyCell(String.valueOf(totals.normal), cellFont));
            summaryTable.addCell(bodyCell(String.valueOf(totals.extra), cellFont));
            summaryTable.addCell(bodyCell(String.valueOf(totals.normal + totals.extra), cellFont));
            normalSum += totals.normal;
            extraSum += totals.extra;
        }

        summaryTable.addCell(headerCell("Total", headerFont));
        summaryTable.addCell(headerCell(String.valueOf(normalSum), headerFont));
        summaryTable.addCell(headerCell(String.valueOf(extraSum), headerFont));
        summaryTable.addCell(headerCell(String.valueOf(normalSum + extraSum), headerFont));

        PdfPTable routesTable = new PdfPTable(2);
        routesTable.setWidthPercentage(85);
        routesTable.setWidths(new float[]{70f, 30f});
        routesTable.addCell(headerCell("Nomenclatura", headerFont));
        routesTable.addCell(headerCell("Total", headerFont));

        routeTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    routesTable.addCell(bodyCell(entry.getKey(), cellFont));
                    routesTable.addCell(bodyCell(String.valueOf(entry.getValue()), cellFont));
                });
        routesTable.addCell(headerCell("Total", headerFont));
        routesTable.addCell(headerCell(String.valueOf(totalViajes), headerFont));

        PdfPTable breakdownTable = new PdfPTable(2);
        breakdownTable.setWidthPercentage(85);
        breakdownTable.setWidths(new float[]{70f, 30f});
        breakdownTable.addCell(headerCell("Desgloce", headerFont));
        breakdownTable.addCell(headerCell("Total de viajes", headerFont));
        breakdownTable.addCell(bodyCell("Viajes normales", cellFont));
        breakdownTable.addCell(bodyCell(String.valueOf(totalNormal), cellFont));
        breakdownTable.addCell(bodyCell("Viajes extras", cellFont));
        breakdownTable.addCell(bodyCell(String.valueOf(totalExtra), cellFont));
        breakdownTable.addCell(headerCell("Total", headerFont));
        breakdownTable.addCell(headerCell(String.valueOf(totalViajes), headerFont));

        document.add(new Paragraph("Resumen de recorridos por chofer", titleFont));

        PdfPTable summaryContainer = new PdfPTable(3);
        summaryContainer.setWidthPercentage(100);
        PdfPCell leftCell = new PdfPCell();
        leftCell.addElement(routesTable);
        leftCell.setBorder(PdfPCell.NO_BORDER);
        PdfPCell rightCell = new PdfPCell();
        rightCell.addElement(breakdownTable);
        rightCell.setBorder(PdfPCell.NO_BORDER);
        PdfPCell summaryCell = new PdfPCell();
        summaryCell.addElement(summaryTable);
        summaryCell.setBorder(PdfPCell.NO_BORDER);
        summaryContainer.addCell(summaryCell);
        summaryContainer.addCell(leftCell);
        summaryContainer.addCell(rightCell);

        document.add(new Paragraph("Total de viajes por recorrido", titleFont));
        document.add(summaryContainer);
    }

    private Map<Long, Map<String, Map<Long, CascadaRowDTO>>> buildRowsMap(List<CascadaWeekItemDTO> items) {
        Map<Long, Map<String, Map<Long, CascadaRowDTO>>> rowsByShift = new HashMap<>();
        for (CascadaWeekItemDTO item : items) {
            rowsByShift
                    .computeIfAbsent(item.getShiftId(), k -> new HashMap<>())
                    .computeIfAbsent(item.getDayKey(), k -> new HashMap<>());
            Map<Long, CascadaRowDTO> byDriver = rowsByShift.get(item.getShiftId()).get(item.getDayKey());
            for (CascadaRowDTO row : item.getRows()) {
                byDriver.put(row.getDriverId(), row);
            }
        }
        return rowsByShift;
    }

    private List<String> orderDays(Set<String> dayKeys) {
        return List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom");
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

    private PdfPCell driverCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private float[] buildShiftTableWidths(int dayCount, int extraColumns) {
        float driverWidth = 20f;
        int dataColumns = dayCount * 4;
        float dataWidth = dataColumns > 0 ? 80f / dataColumns : 80f;
        float extraWidth = dataWidth * 1.5f;
        float total = driverWidth + (dataWidth * dataColumns) + (extraWidth * extraColumns);
        float scale = total > 0f ? 100f / total : 1f;

        float[] widths = new float[1 + dataColumns + extraColumns];
        widths[0] = driverWidth * scale;
        for (int i = 1; i < widths.length - extraColumns; i++) {
            widths[i] = dataWidth * scale;
        }
        for (int i = widths.length - extraColumns; i < widths.length; i++) {
            widths[i] = extraWidth * scale;
        }
        return widths;
    }

    private String formatDriverName(String name, String lastName) {
        String safeName = name == null ? "" : name.trim();
        String safeLast = lastName == null ? "" : lastName.trim();
        String full = (safeName + " " + safeLast).trim();
        return full.isBlank() ? safeLast : full;
    }

    private boolean isFilled(String value) {
        return value != null && !value.isBlank();
    }

    private int countNormal(CascadaRowDTO row, DriverTotals totals) {
        int count = 0;
        if (isFilled(row.getE())) {
            totals.normal += 1;
            count += 1;
        }
        if (isFilled(row.getS())) {
            totals.normal += 1;
            count += 1;
        }
        return count;
    }

    private int countExtra(CascadaRowDTO row, DriverTotals totals) {
        int count = 0;
        if (isFilled(row.getEte())) {
            totals.extra += 1;
            count += 1;
        }
        if (isFilled(row.getSte())) {
            totals.extra += 1;
            count += 1;
        }
        return count;
    }

    private void addRouteCount(Map<String, Integer> totals, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        totals.put(value, totals.getOrDefault(value, 0) + 1);
    }

    private static class DriverTotals {
        private int normal;
        private int extra;
    }
}
