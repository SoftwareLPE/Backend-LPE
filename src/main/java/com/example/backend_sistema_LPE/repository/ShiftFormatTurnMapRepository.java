package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.ShiftFormatTurnMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftFormatTurnMapRepository extends JpaRepository<ShiftFormatTurnMap, Long> {
    List<ShiftFormatTurnMap> findByPlantPlantIdAndFormatTypeFormatTypeId(Long plantId, Long formatTypeId);

    void deleteByPlantPlantIdAndFormatTypeFormatTypeId(Long plantId, Long formatTypeId);
}
