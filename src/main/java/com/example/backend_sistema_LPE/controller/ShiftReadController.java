package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.ShiftDTO;
import com.example.backend_sistema_LPE.service.ShiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plants/{plantId}/shifts")
public class ShiftReadController {
    private final ShiftService shiftService;

    public ShiftReadController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<List<ShiftDTO>> getShifts(@PathVariable Long plantId) {
        return ResponseEntity.ok(shiftService.getShiftsByPlant(plantId));
    }
}
