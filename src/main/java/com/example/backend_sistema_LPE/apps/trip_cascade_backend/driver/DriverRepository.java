package com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverRepository extends JpaRepository<Driver,Long> {

    void deleteByPlantCompanyCompanyId(Long companyId);

    void deleteByPlantPlantId(Long plantId);

    List<Driver> findByShiftsShiftId(Long shiftId);

    List<Driver> findByPlantPlantId(Long plantId);

    List<Driver> findByPlantPlantIdAndActiveTrue(Long plantId);
}
