package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FormatTurnConfigDTO;
import com.example.backend_sistema_LPE.model.FormatTurnConfig;
import com.example.backend_sistema_LPE.repository.FormatTurnConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FormatTurnConfigServiceImpl implements FormatTurnConfigService {
    private final FormatTurnConfigRepository formatTurnConfigRepository;

    public FormatTurnConfigServiceImpl(FormatTurnConfigRepository formatTurnConfigRepository) {
        this.formatTurnConfigRepository = formatTurnConfigRepository;
    }

    @Override
    public List<FormatTurnConfigDTO> getByFormatType(Long formatTypeId) {
        if (formatTypeId == null) {
            throw new RuntimeException("formatTypeId is required");
        }
        return formatTurnConfigRepository.findByFormatTypeFormatTypeId(formatTypeId).stream()
                .sorted(Comparator.comparing(FormatTurnConfig::getDayOfWeek)
                        .thenComparing(FormatTurnConfig::getSortOrder))
                .map(config -> new FormatTurnConfigDTO(
                        config.getTurnConfigId(),
                        config.getFormatType().getFormatTypeId(),
                        config.getDayOfWeek(),
                        config.getTurnName(),
                        config.getSortOrder()
                ))
                .toList();
    }
}
