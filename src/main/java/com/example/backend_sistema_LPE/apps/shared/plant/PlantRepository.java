package com.example.backend_sistema_LPE.apps.shared.plant;

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
    Optional<Plant> findByWialonId(long wialonId);

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

    @Query("""
    select p.plantId
    from Plant p
    where p.wialonUnitsGroupId is not null
""")
    List<Long> findPlantIdsWithWialonUnitsGroupId();

    @Query("""
    select new com.example.backend_sistema_LPE.apps.shared.plant.PlantCatalogRowDTO(
        c.companyId,
        c.companyName,
        p.plantId,
        p.plantName,
        count(distinct s.shiftId),
        case when count(distinct s.shiftId) > 0 then true else false end
    )
    from Plant p
    join p.company c
    left join Shift s on s.plant.plantId = p.plantId
    where (:companyId is null or c.companyId = :companyId)
      and (
        :searchPattern is null
        or lower(c.companyName) like :searchPattern
        or lower(p.plantName) like :searchPattern
        or lower(coalesce(p.location, '')) like :searchPattern
      )
    group by c.companyId, c.companyName, p.plantId, p.plantName
    order by c.companyName asc, p.plantName asc
""")
    List<PlantCatalogRowDTO> findPlantCatalogRows(
            @Param("companyId") Long companyId,
            @Param("searchPattern") String searchPattern
    );

}
