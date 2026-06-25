package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_custom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/format-turn-config")
public class FormatTurnConfigController {
    private final FormatTurnConfigService formatTurnConfigService;

    public FormatTurnConfigController(FormatTurnConfigService formatTurnConfigService) {
        this.formatTurnConfigService = formatTurnConfigService;
    }

    @GetMapping
    public ResponseEntity<List<FormatTurnConfigDTO>> getByFormatType(
            @RequestParam Long formatTypeId,
            @RequestParam(required = false) Long plantId
    ) {
        return ResponseEntity.ok(formatTurnConfigService.getByFormatType(formatTypeId, plantId));
    }
}
