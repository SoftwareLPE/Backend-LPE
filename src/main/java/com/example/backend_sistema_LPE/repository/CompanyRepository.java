package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company,Long> {
}
