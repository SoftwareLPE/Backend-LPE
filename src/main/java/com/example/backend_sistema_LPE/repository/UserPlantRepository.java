package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.dto.AuthResponse;
import com.example.backend_sistema_LPE.dto.PlantCompanyInfoDTO;
import com.example.backend_sistema_LPE.model.UserPlant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPlantRepository extends JpaRepository<UserPlant,Long> {
    boolean existsByUserUserIdAndPlantPlantId(Long userId, Long plantId);

    void deleteByUserUserId(Long userId);

    void deleteByPlantCompanyCompanyId(Long companyId);

    void deleteByPlantPlantId(Long plantId);

    @Query("""
    select new com.example.backend_sistema_LPE.dto.PlantCompanyInfoDTO(
        p.plantId, p.plantName, c.companyId, c.companyName
    )
    from UserPlant up
    join up.plant p
    join p.company c
    where up.user.userId = :userId
    order by up.assignmentDate desc
""")
    List<PlantCompanyInfoDTO> findPlantCompanyInfo(@Param("userId") Long userId, Pageable pageable);

    @Query("""
        select up.plant.plantId
        from UserPlant up
        where up.user.userId = :userId
    """)
    List<Long> findPlantIdsByUserId(@Param("userId") Long userId);



}
