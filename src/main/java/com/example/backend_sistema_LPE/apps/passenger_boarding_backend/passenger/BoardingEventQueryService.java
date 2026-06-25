package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto.BoardingEventViewResponse;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoardingEventQueryService {

    private final BoardingEventRepository boardingEventRepository;
    private final BoardingShiftWindowClassifier boardingShiftWindowClassifier;

    public BoardingEventQueryService(
            BoardingEventRepository boardingEventRepository,
            BoardingShiftWindowClassifier boardingShiftWindowClassifier
    ) {
        this.boardingEventRepository = boardingEventRepository;
        this.boardingShiftWindowClassifier = boardingShiftWindowClassifier;
    }

    public List<BoardingEventViewResponse> findTrips(
            Long plantId,
            Long unitId,
            Long resolvedShiftId,
            String windowType,
            Long fromUnix,
            Long toUnix
    ) {
        Specification<BoardingEvent> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (plantId != null) {
                predicates.add(cb.equal(root.get("plant").get("plantId"), plantId));
            }
            if (unitId != null) {
                predicates.add(cb.equal(root.get("unit").get("unitId"), unitId));
            }
            if (fromUnix != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("boardingTime"), toTimestamp(fromUnix)));
            }
            if (toUnix != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("boardingTime"), toTimestamp(toUnix)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<BoardingEvent> rows = boardingEventRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "boardingTime")
        );

        List<BoardingEventViewResponse> response = new ArrayList<>(rows.size());
        BoardingShiftWindowType requestedWindowType = parseWindowType(windowType);
        for (BoardingEvent row : rows) {
            BoardingShiftWindowResolution shiftWindowResolution = boardingShiftWindowClassifier.classify(row);
            if (resolvedShiftId != null) {
                Long currentResolvedShiftId = shiftWindowResolution.shift() == null
                        ? null
                        : shiftWindowResolution.shift().getShiftId();
                if (!resolvedShiftId.equals(currentResolvedShiftId)) {
                    continue;
                }
            }
            if (requestedWindowType != null && shiftWindowResolution.windowType() != requestedWindowType) {
                continue;
            }
            String resolvedShiftName = shiftWindowResolution.shift() == null
                    ? null
                    : shiftWindowResolution.shift().getShiftName();
            response.add(new BoardingEventViewResponse(
                    row.getBoardingEventId(),
                    row.getReportExecution() == null ? null : row.getReportExecution().getReportExecutionId(),
                    row.getPlant() == null ? null : row.getPlant().getPlantId(),
                    row.getPassengerGroup() == null ? null : row.getPassengerGroup().getPassengerGroupId(),
                    row.getUnit() == null ? null : row.getUnit().getUnitId(),
                    row.getPassenger() == null ? null : row.getPassenger().getPassengerId(),
                    row.getPassenger() == null ? null : row.getPassenger().getWialonPassengerId(),
                    row.getRowNumber(),
                    resolvedShiftName,
                    shiftWindowResolution.shift() == null ? null : shiftWindowResolution.shift().getShiftId(),
                    shiftWindowResolution.windowType().name(),
                    row.getBoardingTime(),
                    row.getAlightingTime(),
                    row.getStartLocationText(),
                    row.getStartLatitude(),
                    row.getStartLongitude(),
                    row.getEndLocationText(),
                    row.getEndLatitude(),
                    row.getEndLongitude(),
                    row.getWialonTagId()
            ));
        }
        return response;
    }

    private BoardingShiftWindowType parseWindowType(String windowType) {
        if (windowType == null || windowType.isBlank()) {
            return null;
        }
        try {
            return BoardingShiftWindowType.valueOf(windowType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid windowType. Allowed values: ENTRY, EXIT, OUT_OF_WINDOW, UNMATCHED_SHIFT");
        }
    }

    private Timestamp toTimestamp(Long unixSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixSeconds));
    }
}
