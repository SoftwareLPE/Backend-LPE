package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSyncRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.ReportExecutionService;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportRequest;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto.ExecuteReportResponse;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlantSyncServiceImpl implements PlantSyncService {

    private static final long DEFAULT_OBJECT_SEC_ID = 1L;

    private final PlantRepository plantRepository;
    private final ReportExecutionService reportExecutionService;

    public PlantSyncServiceImpl(
            PlantRepository plantRepository,
            ReportExecutionService reportExecutionService
    ) {
        this.plantRepository = plantRepository;
        this.reportExecutionService = reportExecutionService;
    }

    @Override
    public ExecuteReportResponse syncPlant(Long plantId, PlantSyncRequest request) {
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Plant not found: " + plantId
                ));

        if (plant.getWialonId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plant does not have wialonId configured: " + plantId
            );
        }

        if (plant.getTemplateId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Plant does not have templateId configured: " + plantId
            );
        }

        PlantSyncRequest safeRequest = request == null ? new PlantSyncRequest() : request;

        ExecuteReportRequest executeRequest = new ExecuteReportRequest();
        executeRequest.setResourceId(plant.getWialonId());
        executeRequest.setTemplateId(plant.getTemplateId());
        executeRequest.setObjectId(plant.getWialonId());
        executeRequest.setObjectSecId(DEFAULT_OBJECT_SEC_ID);
        executeRequest.setIntervalFrom(safeRequest.getIntervalFrom());
        executeRequest.setIntervalTo(safeRequest.getIntervalTo());
        executeRequest.setTableIndex(0);
        executeRequest.setIndexFrom(0);
        executeRequest.setIndexTo(1000);
        executeRequest.setForceRefresh(Boolean.TRUE.equals(safeRequest.getForceRefresh()));

        return reportExecutionService.executeReport(executeRequest);
    }
}
