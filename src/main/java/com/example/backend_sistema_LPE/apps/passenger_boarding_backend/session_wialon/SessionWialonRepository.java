package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.session_wialon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionWialonRepository extends JpaRepository<SessionWialon, Long> {

    Optional<SessionWialon> findTopByIsActiveTrueOrderByCreatedAtDesc();

    Optional<SessionWialon> findTopByOrderByCreatedAtDesc();

    @Modifying
    @Query("update SessionWialon s set s.isActive = false where s.isActive = true")
    int deactivateAllActiveSessions();
}
