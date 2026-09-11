package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.shared.shift.Shift;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
public class BoardingShiftClassifierServiceImpl implements BoardingShiftClassifierService {
    private static final Logger log = LoggerFactory.getLogger(BoardingShiftClassifierServiceImpl.class);
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Ojinaga");
    private static final Duration ENTRY_WINDOW_BEFORE = Duration.ofMinutes(120);
    private static final Duration EXIT_WINDOW_AFTER = Duration.ofMinutes(120);

    private final ShiftRepository shiftRepository;

    public BoardingShiftClassifierServiceImpl(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    @Override
    public BoardingShiftClassificationResult determinePassengerBoardingShift(BoardingEvent event) {
        if (event == null || event.getPlant() == null || event.getPlant().getPlantId() == null) {
            throw new IllegalStateException("No se puede determinar el turno y tipo del abordaje sin la informacion de planta.");
        }
        return determinePassengerBoardingShiftByPlantAndTime(event.getPlant().getPlantId(), event.getBoardingTime());
    }

    @Override
    public BoardingShiftClassificationResult determinePassengerBoardingShiftByPlantAndTime(Long plantId, Timestamp boardingTime) {
        if (plantId == null || boardingTime == null) {
            throw new IllegalStateException("No se puede determinar el turno y tipo del abordaje sin el plantId y la hora del abordaje (boardingTime).");
        }
        List<Shift> shifts = shiftRepository.findByPlantPlantId(plantId).stream()
                .filter(this::isShiftActive)
                .toList();
        if (shifts.isEmpty()) {
            throw new IllegalStateException("Turnos activos no configurados para plantId=" + plantId);
        }
        Optional<ShiftWindowMatch> resolvedMatch = determineBoardingShiftByTimeWindows(shifts, boardingTime);
        if (resolvedMatch.isPresent()) {
            log.info(
                    "Boarding shift classification plantId={} boardingTimestamp={} localBoardingTime={} shiftId={} shiftName={} startTime={} endTime={} eventType={}",
                    plantId,
                    boardingTime,
                    boardingTime.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime(),
                    resolvedMatch.get().shift().getShiftId(),
                    resolvedMatch.get().shift().getShiftName(),
                    resolvedMatch.get().shift().getStartTime(),
                    resolvedMatch.get().shift().getEndTime(),
                    resolvedMatch.get().eventType()
            );
        } else {
            log.warn(
                    "No boarding shift window matched plantId={} boardingTimestamp={} localBoardingTime={} activeShifts={}",
                    plantId,
                    boardingTime,
                    boardingTime.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime(),
                    shifts.stream()
                            .map(shift -> shift.getShiftId() + ":" + shift.getShiftName()
                                    + "[" + shift.getStartTime() + "-" + shift.getEndTime()
                                    + ", days=" + shift.getDayKeys() + "]")
                            .toList()
            );
        }
        return resolvedMatch
                .map(windowMatch -> new BoardingShiftClassificationResult(windowMatch.shift(), windowMatch.eventType()))
                .orElseThrow(() -> new IllegalStateException("No se pudo determinar el turno y tipo del abordaje para plantId=" + plantId));
    }

    private Optional<ShiftWindowMatch> determineBoardingShiftByTimeWindows(List<Shift> shifts, Timestamp boardingTime) {
        LocalDateTime boardingDateTime = boardingTime.toInstant().atZone(DEFAULT_ZONE).toLocalDateTime();
        LocalDate boardingDate = boardingDateTime.toLocalDate();

        List<ShiftWindowMatch> matches = new ArrayList<>();
        for (Shift shift : shifts) {
            if (shift.getStartTime() == null || shift.getEndTime() == null) {
                continue;
            }

            List<ShiftOccurrenceWindow> candidateWindows = buildCandidateWindows(shift, boardingDate);
            for (ShiftOccurrenceWindow candidateWindow : candidateWindows) {
                if (!isShiftScheduledOnDate(shift, candidateWindow.shiftStartDate())) {
                    continue;
                }

                ShiftWindowMatch match = classifyAgainstShiftWindow(
                        shift,
                        boardingDateTime,
                        candidateWindow.shiftStartDateTime(),
                        candidateWindow.shiftEndDateTime()
                );
                if (match != null) {
                    matches.add(match);
                }
            }
        }

        return matches.stream()
                .min(Comparator
                        .comparing((ShiftWindowMatch match) -> match.eventType() == BoardingShiftEventType.ENTRY ? 0 : 1)
                        .thenComparing(match -> match.shift().getShiftId(), Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private List<ShiftOccurrenceWindow> buildCandidateWindows(Shift shift, LocalDate boardingDate) {
        List<ShiftOccurrenceWindow> candidateWindows = new ArrayList<>();
        candidateWindows.add(buildShiftOccurrenceWindow(shift, boardingDate.minusDays(1)));
        candidateWindows.add(buildShiftOccurrenceWindow(shift, boardingDate));
        candidateWindows.add(buildShiftOccurrenceWindow(shift, boardingDate.plusDays(1)));
        return candidateWindows;
    }

    private ShiftOccurrenceWindow buildShiftOccurrenceWindow(Shift shift, LocalDate shiftStartDate) {
        LocalDate shiftEndDate = crossesMidnight(shift) ? shiftStartDate.plusDays(1) : shiftStartDate;
        LocalDateTime shiftStartDateTime = shiftStartDate.atTime(shift.getStartTime());
        LocalDateTime shiftEndDateTime = shiftEndDate.atTime(shift.getEndTime());
        return new ShiftOccurrenceWindow(shiftStartDate, shiftEndDate, shiftStartDateTime, shiftEndDateTime);
    }

    private ShiftWindowMatch classifyAgainstShiftWindow(
            Shift shift,
            LocalDateTime boardingDateTime,
            LocalDateTime shiftStart,
            LocalDateTime shiftEnd
    ) {
        LocalDateTime entryWindowStart = shiftStart.minus(ENTRY_WINDOW_BEFORE);
        LocalDateTime exitWindowEnd = shiftEnd.plus(EXIT_WINDOW_AFTER);
        boolean isEntry = !boardingDateTime.isBefore(entryWindowStart)
                && boardingDateTime.isBefore(shiftStart);
        if (isEntry) {
            return buildWindowMatch(shift, BoardingShiftEventType.ENTRY);
        }

        boolean isExit = !boardingDateTime.isBefore(shiftEnd)
                && !boardingDateTime.isAfter(exitWindowEnd);
        if (isExit) {
            return buildWindowMatch(shift, BoardingShiftEventType.EXIT);
        }

        return null;
    }

    private ShiftWindowMatch buildWindowMatch(Shift shift, BoardingShiftEventType eventType) {
        return new ShiftWindowMatch(shift, eventType);
    }

    private boolean isShiftScheduledOnDate(Shift shift, LocalDate shiftStartDate) {
        if (shift.getDayKeys() == null || shift.getDayKeys().isEmpty()) {
            return false;
        }

        String dayKey = switch (shiftStartDate.getDayOfWeek()) {
            case MONDAY -> "lun";
            case TUESDAY -> "mar";
            case WEDNESDAY -> "mie";
            case THURSDAY -> "jue";
            case FRIDAY -> "vie";
            case SATURDAY -> "sab";
            case SUNDAY -> "dom";
        };

        return shift.getDayKeys().stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(dayKey) || value.equals(dayNameInSpanish(dayKey)));
    }

    private String dayNameInSpanish(String abbreviatedDayKey) {
        return switch (abbreviatedDayKey) {
            case "lun" -> "lunes";
            case "mar" -> "martes";
            case "mie" -> "miercoles";
            case "jue" -> "jueves";
            case "vie" -> "viernes";
            case "sab" -> "sabado";
            case "dom" -> "domingo";
            default -> abbreviatedDayKey;
        };
    }

    private boolean crossesMidnight(Shift shift) {
        return shift.getStartTime() != null
                && shift.getEndTime() != null
                && shift.getEndTime().isBefore(shift.getStartTime());
    }

    private boolean isShiftActive(Shift shift) {
        return shift != null && Boolean.TRUE.equals(shift.getActive());
    }

    private record ShiftOccurrenceWindow(
            LocalDate shiftStartDate,
            LocalDate shiftEndDate,
            LocalDateTime shiftStartDateTime,
            LocalDateTime shiftEndDateTime
    ) {
    }

    private record ShiftWindowMatch(
            Shift shift,
            BoardingShiftEventType eventType
    ) {
    }
}
