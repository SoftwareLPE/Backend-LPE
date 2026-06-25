package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UnitPassengerRowDTO {
    private Long boardingEventId;
    private Long passengerId;
    private String wialonPassengerId;
    private String shift;
    private Timestamp boardingTime;
    private Timestamp alightingTime;
    private String startLocationText;
    private String endLocationText;
}


