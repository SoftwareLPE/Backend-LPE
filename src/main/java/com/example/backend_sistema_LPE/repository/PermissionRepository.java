package com.example.backend_sistema_LPE.repository;


import com.example.backend_sistema_LPE.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> { }

