package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import java.sql.Timestamp;

public interface BoardingShiftClassifierService {
    BoardingShiftClassificationResult determinePassengerBoardingShift(BoardingEvent event);

    BoardingShiftClassificationResult determinePassengerBoardingShiftByPlantAndTime(Long plantId, Timestamp boardingTime);
}
