package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

public final class WeekMetadataResolver {
    private WeekMetadataResolver() {
    }

   public static ResolvedWeekMetadata resolve(
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber
    ) {
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        LocalDate derivedWeekStart = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return resolveAgainstWeekStart(
                weekDate,
                weekStartDate,
                weekEndDate,
                weekNumber,
                derivedWeekStart,
                "weekStartDate must be the Monday for weekDate",
                "weekEndDate must be the Sunday for weekDate",
                "weekNumber must match the ISO week for weekDate"
        );
    }

    public static ResolvedWeekMetadata resolvePreviousWeek(
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber
    ) {
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        LocalDate currentWeekStart = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate derivedWeekStart = currentWeekStart.minusWeeks(1);
        return resolveAgainstWeekStart(
                weekDate,
                weekStartDate,
                weekEndDate,
                weekNumber,
                derivedWeekStart,
                "weekStartDate must be the Monday for the week before weekDate",
                "weekEndDate must be the Sunday for the week before weekDate",
                "weekNumber must match the ISO week before weekDate"
        );
    }

    public static ResolvedWeekMetadata resolveAgainstWeekStart(
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            LocalDate derivedWeekStart,
            String weekStartDateError,
            String weekEndDateError,
            String weekNumberError
    ) {
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        LocalDate derivedWeekEnd = derivedWeekStart.plusDays(6);
        int derivedWeekNumber = derivedWeekStart.get(WeekFields.ISO.weekOfWeekBasedYear());

        if (weekStartDate != null && !weekStartDate.equals(derivedWeekStart)) {
            throw new RuntimeException(weekStartDateError);
        }
        if (weekEndDate != null && !weekEndDate.equals(derivedWeekEnd)) {
            throw new RuntimeException(weekEndDateError);
        }
        if (weekNumber != null && weekNumber != derivedWeekNumber) {
            throw new RuntimeException(weekNumberError);
        }

        return new ResolvedWeekMetadata(
                weekDate,
                weekStartDate != null ? weekStartDate : derivedWeekStart,
                weekEndDate != null ? weekEndDate : derivedWeekEnd,
                weekNumber != null ? weekNumber : derivedWeekNumber
        );
    }

   public static final class ResolvedWeekMetadata {
        private final LocalDate weekDate;
        private final LocalDate weekStartDate;
        private final LocalDate weekEndDate;
        private final int weekNumber;

        private ResolvedWeekMetadata(
                LocalDate weekDate,
                LocalDate weekStartDate,
                LocalDate weekEndDate,
                int weekNumber
        ) {
            this.weekDate = weekDate;
            this.weekStartDate = weekStartDate;
            this.weekEndDate = weekEndDate;
            this.weekNumber = weekNumber;
        }

        public LocalDate getWeekDate() {
            return weekDate;
        }

        public LocalDate getWeekStartDate() {
            return weekStartDate;
        }

        public LocalDate getWeekEndDate() {
            return weekEndDate;
        }

        public int getWeekNumber() {
            return weekNumber;
        }
    }
}
