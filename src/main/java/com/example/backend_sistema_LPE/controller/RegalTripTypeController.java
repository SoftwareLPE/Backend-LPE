package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.RegalTripTypeCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeUpdateRequestDTO;
import com.example.backend_sistema_LPE.service.RegalTripTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/regal-trip-types")
public class RegalTripTypeController {
    private final RegalTripTypeService regalTripTypeService;

    public RegalTripTypeController(RegalTripTypeService regalTripTypeService) {
        this.regalTripTypeService = regalTripTypeService;
    }

    @GetMapping("/plant/{plantId}")
    public ResponseEntity<List<RegalTripTypeDTO>> getTripTypes(@PathVariable Long plantId) {
        return ResponseEntity.ok(regalTripTypeService.getTripTypes(plantId));
    }

    @PostMapping("/plant/{plantId}")
    public ResponseEntity<RegalTripTypeDTO> createTripType(
            @PathVariable Long plantId,
            @RequestBody RegalTripTypeCreateRequestDTO request
    ) {
        return ResponseEntity.ok(regalTripTypeService.createTripType(plantId, request));
    }

    @PutMapping("/plant/{plantId}/{tripTypeId}")
    public ResponseEntity<RegalTripTypeDTO> updateTripType(
            @PathVariable Long plantId,
            @PathVariable Long tripTypeId,
            @RequestBody RegalTripTypeUpdateRequestDTO request
    ) {
        return ResponseEntity.ok(regalTripTypeService.updateTripType(plantId, tripTypeId, request));
    }

    @DeleteMapping("/plant/{plantId}/{tripTypeId}")
    public ResponseEntity<Void> deactivateTripType(
            @PathVariable Long plantId,
            @PathVariable Long tripTypeId
    ) {
        regalTripTypeService.deactivateTripType(plantId, tripTypeId);
        return ResponseEntity.noContent().build();
    }
}
