package com.example.backend_sistema_LPE.apps.shared.shift;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftFormatTurnMapRepository extends JpaRepository<ShiftFormatTurnMap, Long> {
    List<ShiftFormatTurnMap> findByPlantPlantIdAndFormatTypeFormatTypeId(Long plantId, Long formatTypeId);

    List<ShiftFormatTurnMap> findByPlantPlantIdAndFormatTypeFormatTypeIdAndShiftShiftId(
            Long plantId,
            Long formatTypeId,
            Long shiftId
    );

    void deleteByPlantPlantIdAndFormatTypeFormatTypeId(Long plantId, Long formatTypeId);

    void deleteByPlantPlantIdAndFormatTypeFormatTypeIdAndShiftShiftId(
            Long plantId,
            Long formatTypeId,
            Long shiftId
    );
}
