package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import java.util.List;

public interface FlexsurServiceService {
    List<FlexsurServiceDTO> getServices(Long plantId, Boolean active, Long shiftId);

    FlexsurServiceDTO createService(FlexsurServiceCreateRequestDTO request);

    FlexsurServiceDTO updateService(Long serviceId, FlexsurServiceUpdateRequestDTO request);

    void deleteService(Long serviceId);
}
