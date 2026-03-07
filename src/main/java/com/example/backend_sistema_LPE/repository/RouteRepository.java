package com.example.backend_sistema_LPE.repository;

import com.example.backend_sistema_LPE.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route,Long> {
    Optional<Route> findByRouteNameAndPlantPlantId(String routeName, Long plantId);
    List<Route> findByPlantPlantId(Long plantId);
}
