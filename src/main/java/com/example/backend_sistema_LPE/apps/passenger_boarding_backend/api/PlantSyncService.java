package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSyncRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;

public interface PlantSyncService {

    ExecuteReportResponse syncPlant(Long plantId, PlantSyncRequest request);
}
