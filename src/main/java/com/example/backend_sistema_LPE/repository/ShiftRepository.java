package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    List<Shift> findByPlantPlantId(Long plantId);

    Optional<Shift> findByShiftIdAndPlantPlantId(Long shiftId, Long plantId);

    Optional<Shift> findByPlantPlantIdAndShiftName(Long plantId, String shiftName);

    void deleteByPlantPlantId(Long plantId);

    void deleteByPlantCompanyCompanyId(Long companyId);
}
