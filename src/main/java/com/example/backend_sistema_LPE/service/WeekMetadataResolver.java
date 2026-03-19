package com.example.backend_sistema_LPE.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

final class WeekMetadataResolver {
    private WeekMetadataResolver() {
    }

    static ResolvedWeekMetadata resolve(
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber
    ) {
        if (weekDate == null) {
            throw new RuntimeException("weekDate is required");
        }

        LocalDate derivedWeekStart = weekDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate derivedWeekEnd = derivedWeekStart.plusDays(6);
        int derivedWeekNumber = weekDate.get(WeekFields.ISO.weekOfWeekBasedYear());

        if (weekStartDate != null && !weekStartDate.equals(derivedWeekStart)) {
            throw new RuntimeException("weekStartDate must be the Monday for weekDate");
        }
        if (weekEndDate != null && !weekEndDate.equals(derivedWeekEnd)) {
            throw new RuntimeException("weekEndDate must be the Sunday for weekDate");
        }
        if (weekNumber != null && weekNumber != derivedWeekNumber) {
            throw new RuntimeException("weekNumber must match the ISO week for weekDate");
        }

        return new ResolvedWeekMetadata(
                weekDate,
                weekStartDate != null ? weekStartDate : derivedWeekStart,
                weekEndDate != null ? weekEndDate : derivedWeekEnd,
                weekNumber != null ? weekNumber : derivedWeekNumber
        );
    }

    static final class ResolvedWeekMetadata {
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

        LocalDate getWeekDate() {
            return weekDate;
        }

        LocalDate getWeekStartDate() {
            return weekStartDate;
        }

        LocalDate getWeekEndDate() {
            return weekEndDate;
        }

        int getWeekNumber() {
            return weekNumber;
        }
    }
}
