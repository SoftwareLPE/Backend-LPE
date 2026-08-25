package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto;

import java.sql.Timestamp;

public record BoardingEventPdfExportRowDTO(
        Long unitId,
        String unitName,
        String route,
        String wialonPassengerId,
        String shiftName,
        String eventTypeLabel,
        Timestamp originDateTime,
        String originLocation,
        Timestamp destinationDateTime,
        String destinationLocation
) {
}
