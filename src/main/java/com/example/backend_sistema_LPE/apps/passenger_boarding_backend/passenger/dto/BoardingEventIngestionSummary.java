package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardingEventIngestionSummary {
    private int candidateRows;
    private int inserted;
    private int skippedNoCellsArray;
    private int skippedMissingRequiredColumns;
    private int skippedMissingPassengerId;
    private int skippedMissingUnitWialonId;
    private int skippedDuplicateWialonRowKey;
}
