package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.Plant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlantRepository extends JpaRepository<Plant,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select p
    from Plant p
    where p.plantId = :plantId
""")
    Optional<Plant> findByIdForUpdate(@Param("plantId") Long plantId);

    Optional<Plant> findByPlantIdAndCompanyCompanyId(Long plantId, Long companyId);

    @Query("""
    select p.plantId
    from Plant p
    where p.company.companyId in :companyIds
""")
    List<Long> findPlantIdsByCompanyIds(@Param("companyIds") List<Long> companyIds);

    @Query("""
    select p.plantId
    from Plant p
    where p.company.companyId = :companyId
""")
    List<Long> findPlantIdsByCompanyId(@Param("companyId") Long companyId);

    boolean existsByPlantIdAndCompanyCompanyId(Long plantId, Long companyId);

    @Query("""
    select p.company.companyId, count(p.plantId)
    from Plant p
    group by p.company.companyId
""")
    List<Object[]> countPlantsByCompany();

    void deleteByCompanyCompanyId(Long companyId);

}
