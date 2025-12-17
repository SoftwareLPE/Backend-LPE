package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {
    Optional<Route> findByRouteName(String routeName);
}
