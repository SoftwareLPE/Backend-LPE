package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatTurnConfigDTO;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import com.example.backend_sistema_LPE.repository.ShiftFormatTurnMapRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FormatTurnConfigServiceImpl implements FormatTurnConfigService {
    private final FormatTurnConfigRepository formatTurnConfigRepository;
    private final ShiftFormatTurnMapRepository shiftFormatTurnMapRepository;

    public FormatTurnConfigServiceImpl(
            FormatTurnConfigRepository formatTurnConfigRepository,
            ShiftFormatTurnMapRepository shiftFormatTurnMapRepository
    ) {
        this.formatTurnConfigRepository = formatTurnConfigRepository;
        this.shiftFormatTurnMapRepository = shiftFormatTurnMapRepository;
    }

    @Override
    public List<FormatTurnConfigDTO> getByFormatType(Long formatTypeId, Long plantId) {
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }

        Map<String, Long> shiftByDayAndTurn = plantId == null
                ? Map.of()
                : shiftFormatTurnMapRepository.findByPlantPlantIdAndFormatTypeFormatTypeId(plantId, formatTypeId)
                        .stream()
                        .collect(Collectors.toMap(
                                map -> map.getDayOfWeek() + "|" + normalizeTurnName(map.getTurnName()),
                                map -> map.getShift().getShiftId(),
                                (a, b) -> a
                        ));

        List<FormatTurnConfigDTO> configs = formatTurnConfigRepository.findByFormatTypeFormatTypeId(formatTypeId).stream()
                .sorted(Comparator.comparing(FormatTurnConfig::getDayOfWeek)
                        .thenComparing(FormatTurnConfig::getSortOrder))
                .map(config -> new FormatTurnConfigDTO(
                        config.getTurnConfigId(),
                        config.getFormatType().getFormatTypeId(),
                        shiftByDayAndTurn.get(config.getDayOfWeek() + "|" + normalizeTurnName(config.getTurnName())),
                        config.getDayOfWeek(),
                        config.getTurnName(),
                        config.getSortOrder()
                ))
                .toList();

        if (plantId != null && configs.stream().anyMatch(config ->
                config.getShiftId() == null && !isFormatLevelColumn(config.getTurnName()))) {
            throw new RuntimeException("Missing shift mapping for one or more turnConfigId");
        }

        return configs;
    }

    private String normalizeTurnName(String turnName) {
        return turnName == null ? "" : turnName.trim().toLowerCase();
    }

    private boolean isFormatLevelColumn(String turnName) {
        String normalized = normalizeTurnName(turnName).toUpperCase();
        return normalized.startsWith("TE");
    }
}
