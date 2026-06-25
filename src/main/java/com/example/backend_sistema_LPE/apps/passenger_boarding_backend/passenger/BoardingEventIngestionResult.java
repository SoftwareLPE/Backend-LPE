package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardingEventIngestionResult {
    private int candidateRows;
    private int inserted;
    private int skippedNoCellsArray;
    private int skippedMissingRequiredColumns;
    private int skippedMissingPassengerId;
    private int skippedMissingUnitWialonId;
    private int skippedDuplicateWialonRowKey;
}
