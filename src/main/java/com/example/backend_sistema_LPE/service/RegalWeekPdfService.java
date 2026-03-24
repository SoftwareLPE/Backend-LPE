package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalDetailDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekResponseDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekRowDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekSchemaTripTypeDTO;
import com.example.backend_sistema_LPE.dto.RegalWeekTotalsDTO;
import com.example.backend_sistema_LPE.dto.RegalWeeklySummaryDTO;
import com.example.backend_sistema_LPE.model.Plant;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RegalWeekPdfService {
    private static final List<String> ORDERED_DAY_KEYS = List.of("lun", "mar", "mie", "jue", "vie", "sab", "dom");
    private static final Map<String, String> DAY_LABELS = Map.of(
            "lun", "Lunes",
            "mar", "Martes",
            "mie", "Miercoles",
            "jue", "Jueves",
            "vie", "Viernes",
            "sab", "Sabado",
            "dom", "Domingo"
    );

    private static final Color DRIVER_HEADER_COLOR = new Color(67, 109, 164);
    private static final Color NORMAL_SECTION_COLOR = new Color(38, 170, 225);
    private static final Color EXTRA_SECTION_COLOR = new Color(245, 221, 127);
    private static final Color SUMMARY_NORMAL_COLOR = new Color(69, 179, 219);
    private static final Color SUMMARY_EXTRA_COLOR = new Color(246, 223, 133);
    private static final Color SUMMARY_TOTAL_COLOR = new Color(204, 194, 230);
    private static final Color SOFT_BORDER_COLOR = new Color(150, 150, 150);
    private static final float SOFT_BORDER_WIDTH = 0.45f;

    private final RegalWeekService regalWeekService;
    private final PlantRepository plantRepository;

    public RegalWeekPdfService(
            RegalWeekService regalWeekService,
            PlantRepository plantRepository
    ) {
        this.regalWeekService = regalWeekService;
        this.plantRepository = plantRepository;
    }

    public byte[] buildWeeklyPdf(Long plantId, LocalDate weekDate, Long shiftId) {
        if (plantId == null) {
            throw new RuntimeException("plantId is required");
        }
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found"));
        WeekMetadataResolver.ResolvedWeekMetadata weekMetadata = WeekMetadataResolver.resolve(
                weekDate,
                null,
                null,
                null
        );

        RegalWeekSchemaDTO schema = regalWeekService.getRegalWeekSchema(plantId);
        RegalWeekResponseDTO response = regalWeekService.getRegalWeek(
                plantId,
                weekMetadata.getWeekStartDate(),
                shiftId
        );

        List<RegalWeekSchemaTripTypeDTO> tripTypes = schema.getTripTypes() == null ? List.of() : schema.getTripTypes();
        List<RegalWeekRowDTO> rows = response.getRows() == null ? List.of() : response.getRows();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A3.rotate(), 12f, 12f, 18f, 18f);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, plant, weekMetadata);
        addWeeklyTable(document, tripTypes, rows);
        addSummaryTable(document, response.getTotals());

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(
            Document document,
            Plant plant,
            WeekMetadataResolver.ResolvedWeekMetadata weekMetadata
    ) {
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font plantFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, Color.BLACK);
        Font metaLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font metaValueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{54f, 46f});
        wrapper.setSpacingAfter(8f);

        PdfPTable leftTable = new PdfPTable(1);
        leftTable.setWidthPercentage(100);
        leftTable.addCell(titleBlockCell(resolveCompanyName(plant), companyFont));
        leftTable.addCell(titleBlockCell(safeValue(plant.getPlantName()).toUpperCase(new Locale("es", "MX")), plantFont));
        leftTable.addCell(titleBlockCell("REPORTE REGAL", companyFont));

        PdfPTable rightTable = new PdfPTable(2);
        rightTable.setWidthPercentage(100);
        rightTable.setWidths(new float[]{35f, 65f});
        rightTable.addCell(metaLabelCell("SEMANA", metaLabelFont));
        rightTable.addCell(metaValueCell(formatWeekRange(weekMetadata), metaValueFont));
        rightTable.addCell(metaLabelCell("NUMERO", metaLabelFont));
        rightTable.addCell(metaValueCell(String.valueOf(weekMetadata.getWeekNumber()), metaValueFont));

        PdfPCell leftCell = new PdfPCell(leftTable);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingRight(10f);

        PdfPCell rightCell = new PdfPCell(rightTable);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(10f);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        wrapper.addCell(leftCell);
        wrapper.addCell(rightCell);
        document.add(wrapper);
    }

    private void addWeeklyTable(
            Document document,
            List<RegalWeekSchemaTripTypeDTO> tripTypes,
            List<RegalWeekRowDTO> rows
    ) {
        Font groupFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
        Font whiteGroupFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.WHITE);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, Color.BLACK);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 6, Color.BLACK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, Color.BLACK);

        int columns = 1 + (tripTypes.size() * 10) + 1;
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setWidths(buildColumnWidths(tripTypes.size()));
        table.setSpacingAfter(10f);

        PdfPCell driverHeader = headerCell("CHOFER", whiteGroupFont, DRIVER_HEADER_COLOR);
        driverHeader.setRowspan(2);
        table.addCell(driverHeader);

        for (RegalWeekSchemaTripTypeDTO tripType : tripTypes) {
            PdfPCell groupHeader = headerCell(
                    safeValue(tripType.getLabel()),
                    isNormalTripType(tripType) ? groupFont : groupFont,
                    sectionColor(tripType)
            );
            groupHeader.setColspan(10);
            table.addCell(groupHeader);
        }

        PdfPCell sumHeader = headerCell("SUM", groupFont, EXTRA_SECTION_COLOR);
        sumHeader.setRowspan(2);
        table.addCell(sumHeader);

        for (RegalWeekSchemaTripTypeDTO tripType : tripTypes) {
            table.addCell(headerCell("RUTA", headerFont, sectionColor(tripType)));
            table.addCell(headerCell("RECORRIDO", headerFont, sectionColor(tripType)));
            for (String dayKey : ORDERED_DAY_KEYS) {
                table.addCell(headerCell(dayLabel(dayKey), headerFont, sectionColor(tripType)));
            }
            table.addCell(headerCell("TOTAL", headerFont, sectionColor(tripType)));
        }

        for (RegalWeekRowDTO row : rows) {
            table.addCell(driverCell(displayDriver(row), bodyFont));
            Map<Long, Map<String, Integer>> detailsByTripType = detailsByTripType(row);
            for (RegalWeekSchemaTripTypeDTO tripType : tripTypes) {
                Map<String, Integer> byDay = detailsByTripType.getOrDefault(tripType.getTripTypeId(), Map.of());
                table.addCell(bodyCell(row.getRouteName(), bodyFont));
                table.addCell(bodyCell(row.getRecorrido(), bodyFont));
                int sectionTotal = 0;
                for (String dayKey : ORDERED_DAY_KEYS) {
                    int count = byDay.getOrDefault(dayKey, 0);
                    sectionTotal += count;
                    table.addCell(numberCell(count, bodyFont));
                }
                int resolvedSectionTotal = row.getTotalsByTripType() == null
                        ? sectionTotal
                        : row.getTotalsByTripType().getOrDefault(tripType.getTripTypeId(), sectionTotal);
                table.addCell(numberCell(resolvedSectionTotal, totalFont));
            }
            table.addCell(numberCell(row.getRowTotal() == null ? 0 : row.getRowTotal(), totalFont));
        }

        document.add(table);
    }

    private void addSummaryTable(Document document, RegalWeekTotalsDTO totals) {
        RegalWeeklySummaryDTO weeklySummary = totals == null ? null : totals.getWeeklySummary();
        int normalShort = weeklySummary == null || weeklySummary.getNormalShort() == null ? 0 : weeklySummary.getNormalShort();
        int normalLong = weeklySummary == null || weeklySummary.getNormalLong() == null ? 0 : weeklySummary.getNormalLong();
        int extraShort = weeklySummary == null || weeklySummary.getExtraShort() == null ? 0 : weeklySummary.getExtraShort();
        int extraLong = weeklySummary == null || weeklySummary.getExtraLong() == null ? 0 : weeklySummary.getExtraLong();
        int grandTotal = totals == null || totals.getWeekTotal() == null
                ? normalShort + normalLong + extraShort + extraLong
                : totals.getWeekTotal();

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(32);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryTable.setWidths(new float[]{82f, 18f});

        addSummaryRow(summaryTable, "Viajes cortos Normales", normalShort, SUMMARY_NORMAL_COLOR, labelFont, valueFont);
        addSummaryRow(summaryTable, "Viajes largos Normales", normalLong, SUMMARY_NORMAL_COLOR, labelFont, valueFont);
        addSummaryRow(summaryTable, "Viajes cortos T. Extra", extraShort, SUMMARY_EXTRA_COLOR, labelFont, valueFont);
        addSummaryRow(summaryTable, "Viajes largos T. Extra", extraLong, SUMMARY_EXTRA_COLOR, labelFont, valueFont);
        addSummaryRow(summaryTable, "Gran Total", grandTotal, SUMMARY_TOTAL_COLOR, labelFont, valueFont);

        document.add(summaryTable);
    }

    private void addSummaryRow(
            PdfPTable table,
            String label,
            int value,
            Color background,
            Font labelFont,
            Font valueFont
    ) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(background);
        labelCell.setPadding(4f);
        labelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        labelCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(labelCell);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(String.valueOf(value), valueFont));
        valueCell.setBackgroundColor(background);
        valueCell.setPadding(4f);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(valueCell);
        table.addCell(valueCell);
    }

    private Map<Long, Map<String, Integer>> detailsByTripType(RegalWeekRowDTO row) {
        Map<Long, Map<String, Integer>> detailsByTripType = new HashMap<>();
        if (row == null || row.getDetails() == null) {
            return detailsByTripType;
        }
        for (RegalDetailDTO detail : row.getDetails()) {
            if (detail == null || detail.getTripTypeId() == null || detail.getDayOfWeek() == null) {
                continue;
            }
            Map<String, Integer> byDay = detailsByTripType.computeIfAbsent(detail.getTripTypeId(), key -> new HashMap<>());
            byDay.put(detail.getDayOfWeek(), detail.getTripCount() == null ? 0 : detail.getTripCount());
        }
        return detailsByTripType;
    }

    private float[] buildColumnWidths(int tripTypeCount) {
        float[] widths = new float[1 + (tripTypeCount * 10) + 1];
        int index = 0;
        widths[index++] = 14f;
        for (int i = 0; i < tripTypeCount; i++) {
            widths[index++] = 5f;
            widths[index++] = 9f;
            for (int day = 0; day < 7; day++) {
                widths[index++] = 4.2f;
            }
            widths[index++] = 5f;
        }
        widths[index] = 6f;
        return widths;
    }

    private PdfPCell titleBlockCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(0f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell metaLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(cell);
        return cell;
    }

    private PdfPCell metaValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(cell);
        return cell;
    }

    private PdfPCell headerCell(String text, Font font, Color background) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setPadding(3f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(background);
        softenBorders(cell);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setPadding(2.5f);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(cell);
        return cell;
    }

    private PdfPCell driverCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        return cell;
    }

    private PdfPCell numberCell(int value, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(value == 0 ? "" : String.valueOf(value), font));
        cell.setPadding(2.5f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        softenBorders(cell);
        return cell;
    }

    private void softenBorders(PdfPCell cell) {
        cell.setBorderColor(SOFT_BORDER_COLOR);
        cell.setBorderWidth(SOFT_BORDER_WIDTH);
    }

    private boolean isNormalTripType(RegalWeekSchemaTripTypeDTO tripType) {
        String text = (safeValue(tripType.getLabel()) + " " + safeValue(tripType.getCode())).toLowerCase(Locale.ROOT);
        return text.contains("normal");
    }

    private Color sectionColor(RegalWeekSchemaTripTypeDTO tripType) {
        return isNormalTripType(tripType) ? NORMAL_SECTION_COLOR : EXTRA_SECTION_COLOR;
    }

    private String displayDriver(RegalWeekRowDTO row) {
        String firstName = safeValue(row.getDriverName()).trim();
        String lastName = safeValue(row.getDriverLastName()).trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? safeValue(row.getDriverName()) : fullName;
    }

    private String dayLabel(String dayKey) {
        return DAY_LABELS.getOrDefault(dayKey, safeValue(dayKey));
    }

    private String formatWeekRange(WeekMetadataResolver.ResolvedWeekMetadata weekMetadata) {
        return weekMetadata.getWeekStartDate() + " AL " + weekMetadata.getWeekEndDate();
    }

    private String resolveCompanyName(Plant plant) {
        if (plant == null || plant.getCompany() == null || plant.getCompany().getCompanyName() == null) {
            return "";
        }
        return plant.getCompany().getCompanyName();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
