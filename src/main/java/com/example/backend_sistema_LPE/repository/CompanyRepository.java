package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.dto.CompanyTableDTO;
import com.example.backend_sistema_LPE.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {

    @Query("""
      select distinct c
      from Company c
      left join fetch c.plants
      where c.companyId = :companyId
    """)
    Optional<Company> findByIdWithPlants(@Param("companyId") Long companyId);

    @Query("""
   select new com.example.backend_sistema_LPE.dto.CompanyTableDTO(
       c.companyId,
       c.companyName,
       count(p)
   )
   from Company c
   left join c.plants p
   group by c.companyId, c.companyName
   order by c.companyName
""")
    List<CompanyTableDTO> findCompaniesForTable();
}
