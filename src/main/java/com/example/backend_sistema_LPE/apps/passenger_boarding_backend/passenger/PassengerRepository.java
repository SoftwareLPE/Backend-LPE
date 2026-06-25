package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByWialonPassengerId(String wialonPassengerId);
}
