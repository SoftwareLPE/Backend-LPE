package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.RolePlant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePlantRepository extends JpaRepository<RolePlant, Long> {
    void deleteByRoleRoleId(Long roleId);

    @Query("""
    select rp.plant.plantId
    from RolePlant rp
    where rp.role.roleId = :roleId
""")
    List<Long> findPlantIdsByRoleId(@Param("roleId") Long roleId);

    @Query("""
    select rp.role.roleId, rp.plant.plantId, rp.plant.company.companyId
    from RolePlant rp
""")
    List<Object[]> findRolePlantCompanyTriples();

    void deleteByPlantCompanyCompanyId(Long companyId);

    void deleteByPlantPlantId(Long plantId);

}
