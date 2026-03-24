package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.FlexsurServiceCreateRequestDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceDTO;
import com.example.backend_sistema_LPE.dto.FlexsurServiceUpdateRequestDTO;

import java.util.List;

public interface FlexsurServiceService {
    List<FlexsurServiceDTO> getServices(Long plantId, Boolean active, Long shiftId);

    FlexsurServiceDTO createService(FlexsurServiceCreateRequestDTO request);

    FlexsurServiceDTO updateService(Long serviceId, FlexsurServiceUpdateRequestDTO request);

    void deleteService(Long serviceId);
}
