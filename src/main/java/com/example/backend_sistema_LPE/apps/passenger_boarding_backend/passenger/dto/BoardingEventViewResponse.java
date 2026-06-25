package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BoardingEventViewResponse {
    private Long boardingEventId;
    private Long reportExecutionId;
    private Long plantId;
    private Long passengerGroupId;
    private Long unitId;
    private Long passengerId;
    private String wialonPassengerId;
    private String rowNumber;
    private String shiftName;
    private Long resolvedShiftId;
    private String boardingWindowType;
    private Timestamp boardingTime;
    private Timestamp alightingTime;
    private String startLocationText;
    private Double startLatitude;
    private Double startLongitude;
    private String endLocationText;
    private Double endLatitude;
    private Double endLongitude;
    private String wialonTagId;
}
