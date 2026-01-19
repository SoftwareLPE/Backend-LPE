package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    void deleteByRoleRoleId(Long roleId);

    @Query("""
    select rp.permission.permissionId
    from RolePermission rp
    where rp.role.roleId = :roleId
""")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Query("""
    select rp.role.roleId, count(distinct rp.permission.permissionId)
    from RolePermission rp
    group by rp.role.roleId
""")
    List<Object[]> countPermissionsByRole();


}
