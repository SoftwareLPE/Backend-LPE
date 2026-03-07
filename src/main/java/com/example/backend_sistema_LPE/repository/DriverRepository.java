package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.dto.DriverViewDTO;
import com.example.backend_sistema_LPE.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
