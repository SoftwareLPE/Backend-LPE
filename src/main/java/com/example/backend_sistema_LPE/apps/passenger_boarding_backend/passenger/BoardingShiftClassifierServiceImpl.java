package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.shared.shift.Shift;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BoardingShiftClassifierServiceImpl implements BoardingShiftClassifierService {
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
        return determineBoardingShiftByTimeWindows(shifts, boardingTime)
                .map(match -> new BoardingShiftClassificationResult(match.shift(), match.eventType()))
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
                        .comparing((ShiftWindowMatch match) -> !match.exactMatch())
                        .thenComparingLong(match -> match.distanceToWindow().toMillis())
                        .thenComparingLong(match -> match.distanceToBoundary().toMillis())
                        .thenComparing(match -> match.eventType() == BoardingShiftEventType.ENTRY ? 0 : 1)
                        .thenComparing(match -> match.shift().getShiftId()));
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
        ShiftWindowMatch entryMatch = buildWindowMatch(
                shift,
                BoardingShiftEventType.ENTRY,
                boardingDateTime,
                entryWindowStart,
                shiftStart,
                shiftStart
        );
        ShiftWindowMatch exitMatch = buildWindowMatch(
                shift,
                BoardingShiftEventType.EXIT,
                boardingDateTime,
                shiftEnd,
                exitWindowEnd,
                shiftEnd
        );

        if (entryMatch.exactMatch() && !exitMatch.exactMatch()) {
            return entryMatch;
        }
        if (exitMatch.exactMatch() && !entryMatch.exactMatch()) {
            return exitMatch;
        }
        if (entryMatch.exactMatch()) {
            return entryMatch.distanceToBoundary().compareTo(exitMatch.distanceToBoundary()) <= 0
                    ? entryMatch
                    : exitMatch;
        }
        return entryMatch.distanceToWindow().compareTo(exitMatch.distanceToWindow()) <= 0
                ? entryMatch
                : exitMatch;
    }

    private ShiftWindowMatch buildWindowMatch(
            Shift shift,
            BoardingShiftEventType eventType,
            LocalDateTime boardingDateTime,
            LocalDateTime windowStart,
            LocalDateTime windowEnd,
            LocalDateTime boundary
    ) {
        boolean exactMatch = !boardingDateTime.isBefore(windowStart) && !boardingDateTime.isAfter(windowEnd);
        return new ShiftWindowMatch(
                shift,
                eventType,
                exactMatch,
                distanceToWindow(boardingDateTime, windowStart, windowEnd),
                Duration.between(boardingDateTime, boundary).abs()
        );
    }

    private Duration distanceToWindow(LocalDateTime boardingDateTime, LocalDateTime windowStart, LocalDateTime windowEnd) {
        if (!boardingDateTime.isBefore(windowStart) && !boardingDateTime.isAfter(windowEnd)) {
            return Duration.ZERO;
        }
        if (boardingDateTime.isBefore(windowStart)) {
            return Duration.between(boardingDateTime, windowStart).abs();
        }
        return Duration.between(windowEnd, boardingDateTime).abs();
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
            BoardingShiftEventType eventType,
            boolean exactMatch,
            Duration distanceToWindow,
            Duration distanceToBoundary
    ) {
    }
}
