package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.dto.UserTableDTO;
import com.example.backend_sistema_LPE.model.User;
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
        select new com.example.backend_sistema_LPE.dto.UserTableDTO(
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
        select new com.example.backend_sistema_LPE.dto.UserRecipientDTO(
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
    java.util.List<com.example.backend_sistema_LPE.dto.UserRecipientDTO> findRecipientsByRoleName(
            @Param("roleName") String roleName,
            @Param("active") Boolean active
    );
}
