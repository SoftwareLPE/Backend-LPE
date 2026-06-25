package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/format-types")
public class FormatTypeController {
    private final FormatTypeRepository formatTypeRepository;

    public FormatTypeController(FormatTypeRepository formatTypeRepository) {
        this.formatTypeRepository = formatTypeRepository;
    }

    @GetMapping
    public ResponseEntity<List<FormatTypeDTO>> getAll() {
        List<FormatTypeDTO> formats = formatTypeRepository.findAll().stream()
                .map(format -> new FormatTypeDTO(
                        format.getFormatTypeId(),
                        format.getName(),
                        format.getSecondaryColumn(),
                        format.getIncludesUnitType()
                ))
                .toList();
        return ResponseEntity.ok(formats);
    }
}
