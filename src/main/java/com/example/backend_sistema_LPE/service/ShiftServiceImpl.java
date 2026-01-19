package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateShiftRequestDTO;
import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.dto.UpdateShiftRequestDTO;
import com.example.backend_sistema_LPE.model.Shift;
import com.example.backend_sistema_LPE.model.Plant;
import com.example.backend_sistema_LPE.repository.PlantRepository;
import com.example.backend_sistema_LPE.repository.ShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShiftServiceImpl implements ShiftService {
    private final ShiftRepository shiftRepository;
    private final PlantRepository plantRepository;

    public ShiftServiceImpl(ShiftRepository shiftRepository, PlantRepository plantRepository) {
        this.shiftRepository = shiftRepository;
        this.plantRepository = plantRepository;
    }

    @Override
    public List<ShiftDTO> getShiftsByPlant(Long plantId) {
        return shiftRepository.findByPlantPlantId(plantId).stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public ShiftDTO createShift(Long plantId, CreateShiftRequestDTO request) {
        validateRequest(request);
        Plant plant = plantRepository.findById(plantId)
                .orElseThrow(() -> new RuntimeException("Plant not found " + plantId));

        Shift shift = new Shift();
        shift.setPlant(plant);
        shift.setShiftName(request.getShiftName().trim());
        shift.setStartTime(request.getStartTime());
        shift.setEndTime(request.getEndTime());
        shift.setDayKeys(request.getDayKeys());

        return toDTO(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public ShiftDTO updateShift(Long plantId, Long shiftId, UpdateShiftRequestDTO request) {
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));

        boolean hasUpdates = false;

        if (request.getShiftName() != null && !request.getShiftName().trim().isBlank()) {
            shift.setShiftName(request.getShiftName().trim());
            hasUpdates = true;
        }
        if (request.getStartTime() != null) {
            shift.setStartTime(request.getStartTime());
            hasUpdates = true;
        }
        if (request.getEndTime() != null) {
            shift.setEndTime(request.getEndTime());
            hasUpdates = true;
        }
        if (request.getDayKeys() != null && !request.getDayKeys().isEmpty()) {
            shift.setDayKeys(request.getDayKeys());
            hasUpdates = true;
        }

        if (!hasUpdates) {
            return toDTO(shift);
        }

        return toDTO(shiftRepository.save(shift));
    }

    @Override
    @Transactional
    public void deleteShift(Long plantId, Long shiftId) {
        Shift shift = shiftRepository.findByShiftIdAndPlantPlantId(shiftId, plantId)
                .orElseThrow(() -> new RuntimeException("Shift not found " + shiftId));
        shiftRepository.delete(shift);
    }

    private ShiftDTO toDTO(Shift shift) {
        ShiftDTO dto = new ShiftDTO();
        dto.setShiftId(shift.getShiftId());
        dto.setShiftName(shift.getShiftName());
        dto.setDayKeys(shift.getDayKeys());
        dto.setStartTime(shift.getStartTime());
        dto.setEndTime(shift.getEndTime());
        return dto;
    }

    private void validateRequest(CreateShiftRequestDTO request) {
        if (request.getShiftName() == null || request.getShiftName().trim().isBlank()) {
            throw new RuntimeException("shiftName is required");
        }
        if (request.getDayKeys() == null || request.getDayKeys().isEmpty()) {
            throw new RuntimeException("dayKeys is required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("startTime and endTime are required");
        }
    }

    private void validateRequest(UpdateShiftRequestDTO request) {
    }
}
