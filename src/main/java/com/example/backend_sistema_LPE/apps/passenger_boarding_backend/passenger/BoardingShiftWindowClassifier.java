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
public class BoardingShiftWindowClassifier {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Ojinaga");
    private static final Duration ENTRY_WINDOW_BEFORE = Duration.ofMinutes(120);
    private static final Duration EXIT_WINDOW_AFTER = Duration.ofMinutes(120);

    private final ShiftRepository shiftRepository;

    public BoardingShiftWindowClassifier(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    public BoardingShiftWindowResolution classify(BoardingEvent event) {
        if (event == null || event.getPlant() == null || event.getPlant().getPlantId() == null) {
            return new BoardingShiftWindowResolution(null, BoardingShiftWindowType.UNMATCHED_SHIFT);
        }
        return classify(event.getPlant().getPlantId(), event.getBoardingTime());
    }

    public BoardingShiftWindowResolution classify(Long plantId, Timestamp boardingTime) {
        if (plantId == null || boardingTime == null) {
            return new BoardingShiftWindowResolution(null, BoardingShiftWindowType.UNMATCHED_SHIFT);
        }
        List<Shift> shifts = shiftRepository.findByPlantPlantId(plantId);
        if (shifts.isEmpty()) {
            return new BoardingShiftWindowResolution(null, BoardingShiftWindowType.UNMATCHED_SHIFT);
        }
        return resolveByBoardingWindow(shifts, boardingTime)
                .map(match -> new BoardingShiftWindowResolution(match.shift(), match.windowType()))
                .orElseGet(() -> new BoardingShiftWindowResolution(null, BoardingShiftWindowType.OUT_OF_WINDOW));
    }

    private Optional<ShiftWindowMatch> resolveByBoardingWindow(List<Shift> shifts, Timestamp boardingTime) {
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
                        .comparingLong((ShiftWindowMatch match) -> match.distanceToBoundary().toMillis())
                        .thenComparing(match -> match.windowType() == BoardingShiftWindowType.ENTRY ? 0 : 1)
                        .thenComparing(match -> match.shift().getShiftId()));
    }

    private List<ShiftOccurrenceWindow> buildCandidateWindows(Shift shift, LocalDate boardingDate) {
        List<ShiftOccurrenceWindow> candidateWindows = new ArrayList<>();
        candidateWindows.add(buildShiftOccurrenceWindow(shift, boardingDate));
        if (crossesMidnight(shift)) {
            candidateWindows.add(buildShiftOccurrenceWindow(shift, boardingDate.minusDays(1)));
        }
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
        if (!boardingDateTime.isBefore(entryWindowStart) && !boardingDateTime.isAfter(shiftStart)) {
            return new ShiftWindowMatch(
                    shift,
                    BoardingShiftWindowType.ENTRY,
                    Duration.between(boardingDateTime, shiftStart).abs()
            );
        }

        LocalDateTime exitWindowEnd = shiftEnd.plus(EXIT_WINDOW_AFTER);
        if (!boardingDateTime.isBefore(shiftEnd) && !boardingDateTime.isAfter(exitWindowEnd)) {
            return new ShiftWindowMatch(
                    shift,
                    BoardingShiftWindowType.EXIT,
                    Duration.between(shiftEnd, boardingDateTime).abs()
            );
        }

        return null;
    }

    private boolean crossesMidnight(Shift shift) {
        return shift.getStartTime() != null
                && shift.getEndTime() != null
                && shift.getEndTime().isBefore(shift.getStartTime());
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
            BoardingShiftWindowType windowType,
            Duration distanceToBoundary
    ) {
    }
}
