package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api;

import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.PlantSidebarDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitPassengerRowDTO;
import com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto.UnitSummaryDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PassengerMonitoringService {

    List<PlantSidebarDTO> getPlantsSidebar();

    List<UnitSummaryDTO> getUnitsByPlant(Long plantId, Long fromUnix, Long toUnix);

    List<UnitSummaryDTO> getUnitsByPlantWithEvents(Long plantId, Long fromUnix, Long toUnix);

    Page<UnitPassengerRowDTO> getUnitPassengers(Long unitId, Long fromUnix, Long toUnix, String shift, String q, Integer page, Integer size);


}
