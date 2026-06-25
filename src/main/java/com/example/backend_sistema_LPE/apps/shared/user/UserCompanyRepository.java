package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCompanyRepository extends JpaRepository<UserCompany,Long> {

    boolean existsByUserUserIdAndCompanyCompanyId(Long userId, Long companyId);

    List<UserCompany> findByUserUserId(Long userId);

    void deleteByUserUserIdAndCompanyCompanyId(Long userId, Long companyId);

    @Query("""
        select uc.company
        from UserCompany uc
        where uc.user.userId = :userId
    """)
    List<Company> findCompaniesByUserId(@Param("userId") Long userId);

    @Query("""
    select uc.company.companyId
    from UserCompany uc
    where uc.user.userId = :userId
""")
    List<Long> findCompanyIdsByUserId(@Param("userId") Long userId);

    @Query("""
        select new com.example.backend_sistema_LPE.apps.shared.user.UserCompanyAssignmentRowDTO(
            uc.user.userId,
            uc.company.companyId,
            uc.company.companyName
        )
        from UserCompany uc
        where uc.user.userId in :userIds
    """)
    List<UserCompanyAssignmentRowDTO> findCompanyAssignmentsByUserIds(@Param("userIds") List<Long> userIds);


    void deleteByUserUserId(Long userId);

    void deleteByCompanyCompanyId(Long companyId);
}
