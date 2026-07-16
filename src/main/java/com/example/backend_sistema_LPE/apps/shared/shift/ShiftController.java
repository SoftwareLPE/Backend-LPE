package com.example.backend_sistema_LPE.apps.shared.shift;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/plants/{plantId}/shifts")
public class ShiftController {
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping
    public ResponseEntity<List<ShiftDTO>> getShifts(@PathVariable Long plantId) {
        return ResponseEntity.ok(shiftService.getShiftsByPlant(plantId));
    }

    @PostMapping
    public ResponseEntity<ShiftDTO> createShift(
            @PathVariable Long plantId,
            @RequestBody CreateShiftRequestDTO request
    ) {
        return ResponseEntity.ok(shiftService.createShift(plantId, request));
    }

    @PutMapping("/{shiftId}")
    public ResponseEntity<ShiftDTO> updateShift(
            @PathVariable Long plantId,
            @PathVariable Long shiftId,
            @RequestBody UpdateShiftRequestDTO request
    ) {
        return ResponseEntity.ok(shiftService.updateShift(plantId, shiftId, request));
    }

    @PatchMapping("/{shiftId}/status")
    public ResponseEntity<ShiftDTO> updateShiftStatus(
            @PathVariable Long plantId,
            @PathVariable Long shiftId,
            @RequestBody UpdateShiftStatusRequestDTO request
    ) {
        return ResponseEntity.ok(shiftService.updateShiftStatus(plantId, shiftId, request));
    }

    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> deleteShift(
            @PathVariable Long plantId,
            @PathVariable Long shiftId
    ) {
        shiftService.deleteShift(plantId, shiftId);
        return ResponseEntity.noContent().build();
    }
}
