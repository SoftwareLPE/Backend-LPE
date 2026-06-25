package com.example.backend_sistema_LPE.apps.shared.shift;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShiftWialonAliasRepository extends JpaRepository<ShiftWialonAlias, Long> {
    Optional<ShiftWialonAlias> findByShiftPlantPlantIdAndNormalizedAliasName(Long plantId, String normalizedAliasName);
}
