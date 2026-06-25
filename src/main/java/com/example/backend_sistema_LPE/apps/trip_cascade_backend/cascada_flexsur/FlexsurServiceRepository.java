package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.enums.SpecialWeekType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlexsurServiceRepository extends JpaRepository<FlexsurService, Long> {
    List<FlexsurService> findByPlantPlantIdOrderBySortOrderAscServiceNameAsc(Long plantId);

    List<FlexsurService> findByPlantPlantIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(Long plantId);

    List<FlexsurService> findByPlantPlantIdAndShiftShiftIdOrderBySortOrderAscServiceNameAsc(Long plantId, Long shiftId);

    List<FlexsurService> findByPlantPlantIdAndShiftShiftIdAndActiveTrueOrderBySortOrderAscServiceNameAsc(Long plantId, Long shiftId);

    Optional<FlexsurService> findByPlantPlantIdAndServiceNameIgnoreCase(Long plantId, String serviceName);

    Optional<FlexsurService> findByPlantPlantIdAndShiftShiftIdAndServiceTypeIgnoreCaseAndServiceTime(
            Long plantId,
            Long shiftId,
            String serviceType,
            java.time.LocalTime serviceTime
    );

    Optional<FlexsurService> findByPlantPlantIdAndShiftShiftIdAndServiceTypeIgnoreCaseAndServiceTimeAndSpecialWeekType(
            Long plantId,
            Long shiftId,
            String serviceType,
            java.time.LocalTime serviceTime,
            SpecialWeekType specialWeekType
    );
}
