package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.unit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnitCatalogSyncResponseDTO {
    private Long plantId;
    private Long wialonUnitsGroupId;
    private String groupName;
    private int wialonUnitsCount;
    private int createdUnitsCount;
    private int updatedUnitsCount;
    private int activeUnitsCount;
    private int reactivatedUnitsCount;
    private int inactivatedUnitsCount;
    private String message;
}
