package com.example.backend_sistema_LPE.apps.shared.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUserName(String username);

    boolean existsByUserName(String userName);

    boolean existsByEmail(String email);




    @Query("""
        select new com.example.backend_sistema_LPE.apps.shared.user.UserTableDTO(
            u.userId,
            concat(u.name, ' ', u.lastName),
            u.userName,
            u.email,
            u.active,
            r.roleId,
            r.roleName
        )
        from User u
        join u.role r
        where
            (:q is null or :q = '' or
             lower(u.name) like lower(concat('%', :q, '%')) or
             lower(u.lastName) like lower(concat('%', :q, '%')) or
             lower(u.userName) like lower(concat('%', :q, '%')) or
             lower(u.email) like lower(concat('%', :q, '%'))
            )
            and (:roleId is null or r.roleId = :roleId)
            and (:active is null or u.active = :active)
        order by u.createAt desc
    """)
    Page<UserTableDTO> findUsersForTable(
            @Param("q") String q,
            @Param("roleId") Long roleId,
            @Param("active") Boolean active,
            Pageable pageable
    );

    @Query("""
        select new com.example.backend_sistema_LPE.apps.shared.user.UserRecipientDTO(
            u.userId,
            u.name,
            u.lastName,
            u.userName,
            u.email
        )
        from User u
        join u.role r
        where r.roleName = :roleName
          and (:active is null or u.active = :active)
        order by u.name, u.lastName
    """)
    java.util.List<UserRecipientDTO> findRecipientsByRoleName(
            @Param("roleName") String roleName,
            @Param("active") Boolean active
    );

    @Query("""
        select new com.example.backend_sistema_LPE.apps.shared.user.UserRecipientDTO(
            u.userId,
            u.name,
            u.lastName,
            u.userName,
            u.email
        )
        from User u
        join u.role r
        where r.roleKey = :roleKey
          and (:active is null or u.active = :active)
        order by u.name, u.lastName
    """)
    java.util.List<UserRecipientDTO> findRecipientsByRoleKey(
            @Param("roleKey") String roleKey,
            @Param("active") Boolean active
    );
}
