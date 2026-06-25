package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSidebarDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitPassengerRowDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitSummaryDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.BoardingEvent;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.BoardingEventRepository;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.Unit;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit.UnitRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class PassengerMonitoringServiceImpl implements PassengerMonitoringService {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/Ojinaga");
    private static final Pattern EXTRA_UNIT_PATTERN = Pattern.compile("^EXTRA\\s+.+\\s+ID\\s+.+$", Pattern.CASE_INSENSITIVE);

    private final PlantRepository plantRepository;
    private final UnitRepository unitRepository;
    private final BoardingEventRepository boardingEventRepository;

    public PassengerMonitoringServiceImpl(
            PlantRepository plantRepository,
            UnitRepository unitRepository,
            BoardingEventRepository boardingEventRepository
    ) {
        this.plantRepository = plantRepository;
        this.unitRepository = unitRepository;
        this.boardingEventRepository = boardingEventRepository;
    }

    @Override
    public List<PlantSidebarDTO> getPlantsSidebar() {
        List<Plant> plants = plantRepository.findAll(Sort.by(Sort.Direction.ASC, "plantName"));
        List<PlantSidebarDTO> response = new ArrayList<>(plants.size());
        for (Plant plant : plants) {
            long unitsCount = unitRepository.countByPlantPlantIdAndIsActiveTrue(plant.getPlantId());
            response.add(new PlantSidebarDTO(
                    plant.getPlantId(),
                    plant.getPlantName(),
                    unitsCount,
                    plant.getCompany() != null ? plant.getCompany().getCompanyId() : null,
                    plant.getCompany() != null ? plant.getCompany().getCompanyName() : null
            ));
        }
        return response;
    }

    @Override
    public List<UnitSummaryDTO> getUnitsByPlant(Long plantId, Long fromUnix, Long toUnix) {
        List<Unit> units;
        if (fromUnix != null || toUnix != null) {
            IntervalRange intervalRange = resolveIntervalRange(fromUnix, toUnix);
            units = boardingEventRepository.findDistinctUnitsByPlantAndBoardingTimeBetween(
                    plantId,
                    intervalRange.fromUnix() == null ? null : toTimestamp(intervalRange.fromUnix()),
                    intervalRange.toUnix() == null ? null : toTimestamp(intervalRange.toUnix())
            );
        } else {
            units = unitRepository.findAllByPlantPlantIdAndIsActiveTrueOrderByNameRawAsc(plantId);
        }
        units = sortUnits(units);
        List<UnitSummaryDTO> response = new ArrayList<>(units.size());
        for (Unit unit : units) {
            response.add(new UnitSummaryDTO(
                    unit.getUnitId(),
                    unit.getNameRaw(),
                    unit.getInternalId(),
                    unit.getRouteCode(),
                    unit.getRouteName(),
                    unit.getWialonId()
            ));
        }
        return response;
    }

    @Override
    public List<UnitSummaryDTO> getUnitsByPlantWithEvents(Long plantId, Long fromUnix, Long toUnix) {
        IntervalRange intervalRange = resolveIntervalRange(fromUnix, toUnix);
        List<Unit> units = boardingEventRepository.findDistinctUnitsWithEventsByPlantAndBoardingTimeBetween(
                plantId,
                intervalRange.fromUnix() == null ? null : toTimestamp(intervalRange.fromUnix()),
                intervalRange.toUnix() == null ? null : toTimestamp(intervalRange.toUnix())
        );
        units = sortUnits(units);
        List<UnitSummaryDTO> response = new ArrayList<>(units.size());
        for (Unit unit : units) {
            response.add(new UnitSummaryDTO(
                    unit.getUnitId(),
                    unit.getNameRaw(),
                    unit.getInternalId(),
                    unit.getRouteCode(),
                    unit.getRouteName(),
                    unit.getWialonId()
            ));
        }
        return response;
    }

    private List<Unit> sortUnits(List<Unit> units) {
        return units.stream()
                .sorted(java.util.Comparator
                        .comparing((Unit unit) -> isExtraUnit(unit.getNameRaw()))
                        .thenComparing(unit -> normalizeUnitName(unit.getNameRaw()), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean isExtraUnit(String nameRaw) {
        return nameRaw != null && EXTRA_UNIT_PATTERN.matcher(nameRaw.trim()).matches();
    }

    private String normalizeUnitName(String nameRaw) {
        return nameRaw == null ? "" : nameRaw.trim();
    }

    @Override
    public Page<UnitPassengerRowDTO> getUnitPassengers(
            Long unitId,
            Long fromUnix,
            Long toUnix,
            String shift,
            String q,
            Integer page,
            Integer size
    ) {
        IntervalRange intervalRange = resolveIntervalRange(fromUnix, toUnix);
        Long effectiveFromUnix = intervalRange.fromUnix();
        Long effectiveToUnix = intervalRange.toUnix();

        int pageNumber = page == null || page < 0 ? 0 : page;
        int pageSize = size == null || size <= 0 ? 50 : size;
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "boardingTime"));

        Specification<BoardingEvent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("unit").get("unitId"), unitId));

            if (effectiveFromUnix != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("boardingTime"), toTimestamp(effectiveFromUnix)));
            }
            if (effectiveToUnix != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("boardingTime"), toTimestamp(effectiveToUnix)));
            }
            if (shift != null && !shift.isBlank()) {
                predicates.add(cb.equal(root.get("shift"), shift.trim()));
            }
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("passenger").get("wialonPassengerId")), pattern));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<BoardingEvent> rows = boardingEventRepository.findAll(spec, pageable);
        List<UnitPassengerRowDTO> mapped = rows.getContent().stream()
                .map(row -> new UnitPassengerRowDTO(
                        row.getBoardingEventId(),
                        row.getPassenger() == null ? null : row.getPassenger().getPassengerId(),
                        row.getPassenger() == null ? null : row.getPassenger().getWialonPassengerId(),
                        row.getShift(),
                        row.getBoardingTime(),
                        row.getAlightingTime(),
                        row.getStartLocationText(),
                        row.getEndLocationText()
                ))
                .toList();

        return new PageImpl<>(mapped, pageable, rows.getTotalElements());
    }

    private Timestamp toTimestamp(Long unixSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixSeconds));
    }

    private IntervalRange resolveIntervalRange(Long fromUnix, Long toUnix) {
        if (fromUnix != null && toUnix != null) {
            return new IntervalRange(fromUnix, toUnix);
        }

        if (fromUnix != null) {
            LocalDate day = Instant.ofEpochSecond(fromUnix).atZone(DEFAULT_ZONE).toLocalDate();
            long dayEnd = day.plusDays(1).atStartOfDay(DEFAULT_ZONE).toEpochSecond() - 1;
            return new IntervalRange(fromUnix, Math.max(fromUnix, dayEnd));
        }

        if (toUnix != null) {
            LocalDate day = Instant.ofEpochSecond(toUnix).atZone(DEFAULT_ZONE).toLocalDate();
            long dayStart = day.atStartOfDay(DEFAULT_ZONE).toEpochSecond();
            return new IntervalRange(Math.min(dayStart, toUnix), toUnix);
        }

        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        long dayStart = today.atStartOfDay(DEFAULT_ZONE).toEpochSecond();
        long dayEnd = today.plusDays(1).atStartOfDay(DEFAULT_ZONE).toEpochSecond() - 1;
        return new IntervalRange(dayStart, dayEnd);
    }

    private record IntervalRange(Long fromUnix, Long toUnix) {}


}
