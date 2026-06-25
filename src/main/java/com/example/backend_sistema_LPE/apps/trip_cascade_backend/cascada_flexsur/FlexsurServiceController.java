package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flexsur-services")
public class FlexsurServiceController {
    private final FlexsurServiceService flexsurServiceService;

    public FlexsurServiceController(FlexsurServiceService flexsurServiceService) {
        this.flexsurServiceService = flexsurServiceService;
    }

    @GetMapping
    public ResponseEntity<List<FlexsurServiceDTO>> getServices(
            @RequestParam Long plantId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Long shiftId
    ) {
        return ResponseEntity.ok(flexsurServiceService.getServices(plantId, active, shiftId));
    }

    @PostMapping
    public ResponseEntity<FlexsurServiceDTO> createService(
            @RequestBody FlexsurServiceCreateRequestDTO request
    ) {
        return ResponseEntity.ok(flexsurServiceService.createService(request));
    }

    @PatchMapping("/{serviceId}")
    public ResponseEntity<FlexsurServiceDTO> updateService(
            @PathVariable Long serviceId,
            @RequestBody FlexsurServiceUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(flexsurServiceService.updateService(serviceId, request));
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable Long serviceId) {
        flexsurServiceService.deleteService(serviceId);
        return ResponseEntity.noContent().build();
    }
}
