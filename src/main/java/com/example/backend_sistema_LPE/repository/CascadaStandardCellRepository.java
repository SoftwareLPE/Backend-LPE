package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.CascadaStandardCell;
import com.example.backend_sistema_LPE.model.CascadaStandardWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface CascadaStandardCellRepository extends JpaRepository<CascadaStandardCell, Long> {
    List<CascadaStandardCell> findByWeekAndDayKey(CascadaStandardWeek week, String dayKey);

    List<CascadaStandardCell> findByWeek(CascadaStandardWeek week);

    void deleteByWeekAndDayKeyIn(CascadaStandardWeek week, Collection<String> dayKeys);

    List<CascadaStandardCell> findByManualRowManualStandardRowIdIn(List<Long> manualRowIds);
}
