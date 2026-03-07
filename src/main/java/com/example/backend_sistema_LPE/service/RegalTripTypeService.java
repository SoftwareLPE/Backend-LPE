package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RegalTripTypeCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeDTO;
import com.example.backend_sistema_LPE.dto.RegalTripTypeUpdateRequestDTO;

import java.util.List;

public interface RegalTripTypeService {
    List<RegalTripTypeDTO> getTripTypes(Long plantId);

    RegalTripTypeDTO createTripType(Long plantId, RegalTripTypeCreateRequestDTO request);

    RegalTripTypeDTO updateTripType(Long plantId, Long tripTypeId, RegalTripTypeUpdateRequestDTO request);

    void deactivateTripType(Long plantId, Long tripTypeId);
}
