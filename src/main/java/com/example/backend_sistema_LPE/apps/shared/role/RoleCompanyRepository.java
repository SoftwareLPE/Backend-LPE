package com.example.backend_sistema_LPE.apps.shared.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleCompanyRepository extends JpaRepository<RoleCompany, Long> {
    void deleteByRoleRoleId(Long roleId);

    @Query("""
    select rc.company.companyId
    from RoleCompany rc
    where rc.role.roleId = :roleId
""")
    List<Long> findCompanyIdsByRoleId(@Param("roleId") Long roleId);

    @Query("""
    select rc.role.roleId, rc.company.companyId
    from RoleCompany rc
""")
    List<Object[]> findRoleCompaniesPairs();

    void deleteByCompanyCompanyId(Long companyId);

}
