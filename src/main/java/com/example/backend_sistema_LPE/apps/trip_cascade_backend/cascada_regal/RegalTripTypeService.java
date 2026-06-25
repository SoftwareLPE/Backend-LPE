package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import java.util.List;

public interface RegalTripTypeService {
    List<RegalTripTypeDTO> getTripTypes(Long plantId);

    RegalTripTypeDTO createTripType(Long plantId, RegalTripTypeCreateRequestDTO request);

    RegalTripTypeDTO updateTripType(Long plantId, Long tripTypeId, RegalTripTypeUpdateRequestDTO request);

    void deactivateTripType(Long plantId, Long tripTypeId);
}
