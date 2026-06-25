package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CascadaStandardManualRowRepository extends JpaRepository<CascadaStandardManualRow, Long> {
    List<CascadaStandardManualRow> findByPlantPlantIdAndWeekDateOrderBySortOrderAscManualStandardRowIdAsc(
            Long plantId,
            LocalDate weekDate
    );

    void deleteByPlantCompanyCompanyId(Long companyId);
}
