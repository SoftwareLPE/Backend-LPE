package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CascadaResponseDTO;
import com.example.backend_sistema_LPE.dto.CascadaRowDTO;
import com.example.backend_sistema_LPE.dto.CascadaSaveRequestDTO;
import com.example.backend_sistema_LPE.model.CascadaRow;
import com.example.backend_sistema_LPE.model.Driver;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.CascadaRowRepository;
import com.example.backend_sistema_LPE.repository.DriverRepository;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CascadaServiceImpl implements CascadaService {
    private final CascadaRowRepository cascadaRowRepository;
    private final PlantRepository plantRepository;
    private final DriverRepository driverRepository;

    public CascadaServiceImpl(
            CascadaRowRepository cascadaRowRepository,
            PlantRepository plantRepository,
            DriverRepository driverRepository
    ) {
        this.cascadaRowRepository = cascadaRowRepository;
        this.plantRepository = plantRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public CascadaResponseDTO getCascada(Long plantId, LocalDate weekStartDate, String shiftId, String dayKey) {
        List<CascadaRow> rows = cascadaRowRepository
                .findByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
                        plantId,
                        weekStartDate,
                        shiftId,
                        dayKey
                );

        List<CascadaRowDTO> rowDTOs = rows.stream()
                .map(this::toRowDTO)
                .toList();

        CascadaResponseDTO response = new CascadaResponseDTO();
        response.setPlantId(plantId);
        response.setWeekStartDate(weekStartDate);
        response.setShiftId(shiftId);
        response.setDayKey(dayKey);
        response.setRows(rowDTOs);
        return response;
    }

    @Override
    @Transactional
    public void saveCascada(CascadaSaveRequestDTO request) {
        if (request.getPlantId() == null) {
            throw new RuntimeException("plantId is required");
        }
        if (request.getWeekStartDate() == null) {
            throw new RuntimeException("weekStartDate is required");
        }
        if (request.getShiftId() == null || request.getShiftId().isBlank()) {
            throw new RuntimeException("shiftId is required");
        }
        if (request.getDayKey() == null || request.getDayKey().isBlank()) {
            throw new RuntimeException("dayKey is required");
        }
        if (request.getRows() == null) {
            throw new RuntimeException("rows is required");
        }

        Plant plant = plantRepository.findById(request.getPlantId())
                .orElseThrow(() -> new RuntimeException("Plant not found"));

        List<Long> driverIds = request.getRows().stream()
                .map(CascadaRowDTO::getDriverId)
                .distinct()
                .toList();

        Map<Long, Driver> driversById = driverRepository.findAllById(driverIds).stream()
                .collect(Collectors.toMap(Driver::getDriverId, d -> d));

        for (Long driverId : driverIds) {
            if (!driversById.containsKey(driverId)) {
                throw new RuntimeException("Driver not found: " + driverId);
            }
        }

        cascadaRowRepository.deleteByPlantPlantIdAndWeekStartDateAndShiftIdAndDayKey(
                request.getPlantId(),
                request.getWeekStartDate(),
                request.getShiftId(),
                request.getDayKey()
        );

        List<CascadaRow> entities = new ArrayList<>();
        for (CascadaRowDTO rowDTO : request.getRows()) {
            Driver driver = driversById.get(rowDTO.getDriverId());
            CascadaRow row = new CascadaRow();
            row.setPlant(plant);
            row.setDriver(driver);
            row.setWeekStartDate(request.getWeekStartDate());
            row.setShiftId(request.getShiftId());
            row.setDayKey(request.getDayKey());
            row.setEValue(normalizeValue(rowDTO.getE()));
            row.setSValue(normalizeValue(rowDTO.getS()));
            row.setEteValue(normalizeValue(rowDTO.getEte()));
            row.setSteValue(normalizeValue(rowDTO.getSte()));
            entities.add(row);
        }

        cascadaRowRepository.saveAll(entities);
    }

    private CascadaRowDTO toRowDTO(CascadaRow row) {
        CascadaRowDTO dto = new CascadaRowDTO();
        dto.setDriverId(row.getDriver().getDriverId());
        dto.setE(row.getEValue());
        dto.setS(row.getSValue());
        dto.setEte(row.getEteValue());
        dto.setSte(row.getSteValue());
        return dto;
    }

    private String normalizeValue(String value) {
        return value == null ? "" : value;
    }
}
