package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapDTO;
import com.example.backend_sistema_LPE.dto.ShiftFormatTurnMapSaveRequestDTO;
import com.example.backend_sistema_LPE.service.ShiftFormatTurnMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/format-turn-mapping")
public class ShiftFormatTurnMapController {
    private final ShiftFormatTurnMapService shiftFormatTurnMapService;

    public ShiftFormatTurnMapController(ShiftFormatTurnMapService shiftFormatTurnMapService) {
        this.shiftFormatTurnMapService = shiftFormatTurnMapService;
    }

    @GetMapping
    public ResponseEntity<List<ShiftFormatTurnMapDTO>> getMappings(
            @RequestParam Long plantId,
            @RequestParam Long formatTypeId
    ) {
        return ResponseEntity.ok(shiftFormatTurnMapService.getMappings(plantId, formatTypeId));
    }

    @PutMapping
    public ResponseEntity<Void> saveMappings(@RequestBody ShiftFormatTurnMapSaveRequestDTO request) {
        shiftFormatTurnMapService.saveMappings(request);
        return ResponseEntity.noContent().build();
    }
}
