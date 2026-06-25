package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common.WeekMetadataResolver;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FlexsurWeekPdfService {
    private static final Locale SPANISH_LOCALE = new Locale("es", "MX");
    private static final DateTimeFormatter PDF_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yy");

    private final FlexsurWeekService flexsurWeekService;
    private final PlantRepository plantRepository;

    public FlexsurWeekPdfService(
            FlexsurWeekService flexsurWeekService,
            PlantRepository plantRepository
    ) {
        this.flexsurWeekService = flexsurWeekService;
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

        FlexsurWeekResponseDTO response = flexsurWeekService.getFlexsurWeek(
                plantId,
                weekMetadata.getWeekStartDate(),
                shiftId
        );

        List<FlexsurWeekRowDTO> rows = response.getRows() == null ? List.of() : response.getRows();
        Map<String, Integer> byDay = response.getTotals() == null || response.getTotals().getByDay() == null
                ? Map.of()
                : response.getTotals().getByDay();
        int weekTotal = response.getTotals() == null || response.getTotals().getWeekTotal() == null
                ? calculateWeekTotal(rows)
                : response.getTotals().getWeekTotal();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 18f, 18f, 22f, 22f);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        addHeader(document, plant, weekMetadata);
        addThreeDayTable(
                document,
                rows,
                List.of(
                        weekMetadata.getWeekStartDate(),
                        weekMetadata.getWeekStartDate().plusDays(1),
                        weekMetadata.getWeekStartDate().plusDays(2)
                ),
                byDay
        );
        addThreeDayTable(
                document,
                rows,
                List.of(
                        weekMetadata.getWeekStartDate().plusDays(3),
                        weekMetadata.getWeekStartDate().plusDays(4),
                        weekMetadata.getWeekStartDate().plusDays(5)
                ),
                byDay
        );
        addSundayTable(
                document,
                rows,
                weekMetadata.getWeekStartDate().plusDays(6),
                byDay,
                weekTotal
        );

        document.close();
        return outputStream.toByteArray();
    }

    private void addHeader(
            Document document,
            Plant plant,
            WeekMetadataResolver.ResolvedWeekMetadata weekMetadata
    ) {
        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
        Font plantFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
        Font metaLabelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        Font metaValueFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

        PdfPTable wrapper = new PdfPTable(2);
        wrapper.setWidthPercentage(100);
        wrapper.setWidths(new float[]{52f, 48f});
        wrapper.setSpacingAfter(10f);

        PdfPTable leftTable = new PdfPTable(1);
        leftTable.setWidthPercentage(100);
        leftTable.addCell(titleBlockCell(resolveCompanyName(plant), companyFont));
        leftTable.addCell(titleBlockCell(safeValue(plant.getPlantName()).toUpperCase(SPANISH_LOCALE), plantFont));
        leftTable.addCell(titleBlockCell("REPORTE FLEXSUR", companyFont));

        PdfPTable rightTable = new PdfPTable(2);
        rightTable.setWidthPercentage(100);
        rightTable.setWidths(new float[]{34f, 66f});
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

    private void addThreeDayTable(
            Document document,
            List<FlexsurWeekRowDTO> rows,
            List<LocalDate> dates,
            Map<String, Integer> byDay
    ) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);

        PdfPTable table = new PdfPTable(1 + dates.size() * 2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{46f, 9f, 9f, 9f, 9f, 9f, 9f});
        table.setSpacingAfter(14f);

        PdfPCell servicesHeader = headerCell("SERVICIOS", headerFont);
        servicesHeader.setRowspan(2);
        table.addCell(servicesHeader);

        for (LocalDate date : dates) {
            table.addCell(headerCell(dayDisplayName(date), headerFont));
            table.addCell(headerCell(formatPdfDate(date), headerFont));
        }

        for (int i = 0; i < dates.size(); i++) {
            table.addCell(headerCell("VIAJES", headerFont));
            table.addCell(headerCell("TOTAL", headerFont));
        }

        for (FlexsurWeekRowDTO row : rows) {
            table.addCell(serviceCell(row.getServiceName(), bodyFont));
            Map<LocalDate, FlexsurDetailDTO> detailsByDate = detailsByDate(row);
            for (LocalDate date : dates) {
                FlexsurDetailDTO detail = detailsByDate.get(date);
                table.addCell(bodyCell(displayTrips(detail), bodyFont));
                table.addCell(bodyCell(displayTotal(detail), bodyFont));
            }
        }

        table.addCell(bodyCell("", totalFont));
        for (LocalDate date : dates) {
            table.addCell(totalCell("TOTAL DEL DIA", totalFont));
            table.addCell(totalCell(displayDayTotal(byDay, rows, date), totalFont));
        }

        document.add(table);
    }

    private void addSundayTable(
            Document document,
            List<FlexsurWeekRowDTO> rows,
            LocalDate sunday,
            Map<String, Integer> byDay,
            int weekTotal
    ) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.BLACK);
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.BLACK);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{46f, 14f, 14f, 26f});

        PdfPCell servicesHeader = headerCell("SERVICIOS", headerFont);
        servicesHeader.setRowspan(2);
        table.addCell(servicesHeader);
        table.addCell(headerCell(dayDisplayName(sunday), headerFont));
        table.addCell(headerCell(formatPdfDate(sunday), headerFont));
        PdfPCell totalSemanalHeader = headerCell("TOTAL SEMANAL", headerFont);
        totalSemanalHeader.setRowspan(2);
        table.addCell(totalSemanalHeader);

        table.addCell(headerCell("VIAJES", headerFont));
        table.addCell(headerCell("TOTAL", headerFont));

        for (FlexsurWeekRowDTO row : rows) {
            table.addCell(serviceCell(row.getServiceName(), bodyFont));
            FlexsurDetailDTO sundayDetail = detailsByDate(row).get(sunday);
            table.addCell(bodyCell(displayTrips(sundayDetail), bodyFont));
            table.addCell(bodyCell(displayTotal(sundayDetail), bodyFont));
            table.addCell(totalCell(displayWeeklyRowTotal(row), bodyFont));
        }

        table.addCell(bodyCell("", totalFont));
        table.addCell(totalCell("TOTAL DEL DIA", totalFont));
        table.addCell(totalCell(displayDayTotal(byDay, rows, sunday), totalFont));
        table.addCell(totalCell(weekTotal == 0 ? "" : String.valueOf(weekTotal), totalFont));

        document.add(table);
    }

    private Map<LocalDate, FlexsurDetailDTO> detailsByDate(FlexsurWeekRowDTO row) {
        Map<LocalDate, FlexsurDetailDTO> detailsByDate = new HashMap<>();
        if (row == null || row.getDetails() == null) {
            return detailsByDate;
        }
        for (FlexsurDetailDTO detail : row.getDetails()) {
            if (detail != null && detail.getServiceDate() != null) {
                detailsByDate.put(detail.getServiceDate(), detail);
            }
        }
        return detailsByDate;
    }

    private int calculateWeekTotal(List<FlexsurWeekRowDTO> rows) {
        if (rows == null) {
            return 0;
        }
        return rows.stream()
                .map(FlexsurWeekRowDTO::getRowTotal)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String displayTrips(FlexsurDetailDTO detail) {
        if (detail == null || detail.getTrips() == null || detail.getTrips() == 0) {
            return "";
        }
        return String.valueOf(detail.getTrips());
    }

    private String displayTotal(FlexsurDetailDTO detail) {
        if (detail == null) {
            return "";
        }
        int total = detail.getTotal() == null
                ? (detail.getTrips() == null ? 0 : detail.getTrips()) + (detail.getExtraColumn() == null ? 0 : detail.getExtraColumn())
                : detail.getTotal();
        return total == 0 ? "" : String.valueOf(total);
    }

    private String displayWeeklyRowTotal(FlexsurWeekRowDTO row) {
        if (row == null) {
            return "0";
        }
        int total = row.getRowTotal() == null ? 0 : row.getRowTotal();
        return String.valueOf(total);
    }

    private String displayDayTotal(Map<String, Integer> byDay, List<FlexsurWeekRowDTO> rows, LocalDate date) {
        String key = date.toString();
        Integer total = byDay == null ? null : byDay.get(key);
        if (total == null) {
            total = calculateDayTotal(rows, date);
        }
        return total == null || total == 0 ? "" : String.valueOf(total);
    }

    private int calculateDayTotal(List<FlexsurWeekRowDTO> rows, LocalDate date) {
        if (rows == null || date == null) {
            return 0;
        }
        int total = 0;
        for (FlexsurWeekRowDTO row : rows) {
            FlexsurDetailDTO detail = detailsByDate(row).get(date);
            if (detail == null) {
                continue;
            }
            int detailTotal = detail.getTotal() == null
                    ? (detail.getTrips() == null ? 0 : detail.getTrips()) + (detail.getExtraColumn() == null ? 0 : detail.getExtraColumn())
                    : detail.getTotal();
            total += detailTotal;
        }
        return total;
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
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell metaValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4f);
        return cell;
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        cell.setBackgroundColor(Color.WHITE);
        return cell;
    }

    private PdfPCell bodyCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(safeValue(text), font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(3f);
        return cell;
    }

    private PdfPCell serviceCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingLeft(4f);
        return cell;
    }

    private PdfPCell totalCell(String text, Font font) {
        PdfPCell cell = bodyCell(text, font);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private String resolveCompanyName(Plant plant) {
        if (plant == null || plant.getCompany() == null || plant.getCompany().getCompanyName() == null) {
            return "";
        }
        return plant.getCompany().getCompanyName();
    }

    private String formatWeekRange(WeekMetadataResolver.ResolvedWeekMetadata weekMetadata) {
        return formatPdfDate(weekMetadata.getWeekStartDate()) + " AL " + formatPdfDate(weekMetadata.getWeekEndDate());
    }

    private String dayDisplayName(LocalDate date) {
        return date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, SPANISH_LOCALE)
                .toUpperCase(SPANISH_LOCALE);
    }

    private String formatPdfDate(LocalDate date) {
        return date == null ? "" : PDF_DATE_FORMAT.format(date);
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
