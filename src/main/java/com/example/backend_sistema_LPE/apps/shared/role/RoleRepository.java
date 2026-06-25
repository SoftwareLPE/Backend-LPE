package com.example.backend_sistema_LPE.apps.shared.role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {

    boolean existsByRoleName(String roleName);
    boolean existsByRoleKey(String roleKey);

    void deleteById(Long roleId);

}
