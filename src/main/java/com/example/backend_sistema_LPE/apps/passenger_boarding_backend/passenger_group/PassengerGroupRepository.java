package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger_group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerGroupRepository extends JpaRepository<PassengerGroup, Long> {
    Optional<PassengerGroup> findByPlantPlantIdAndWialonId(Long plantId, Long wialonId);
}
