package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report;

public interface BoardingEventPdfExportService {
    byte[] exportPlantBoardingEventsPdf(
            Long plantId,
            Long from,
            Long to,
            Long resolvedShiftId,
            String windowType
    );
}
