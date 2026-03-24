package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.FlexsurDriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlexsurDriverAssignmentRepository extends JpaRepository<FlexsurDriverAssignment, Long> {
    List<FlexsurDriverAssignment> findByPlantPlantId(Long plantId);

    List<FlexsurDriverAssignment> findByPlantPlantIdAndShiftShiftId(Long plantId, Long shiftId);

    Optional<FlexsurDriverAssignment> findByPlantPlantIdAndDriverDriverIdAndServiceServiceIdAndShiftShiftId(
            Long plantId,
            Long driverId,
            Long serviceId,
            Long shiftId
    );
}
