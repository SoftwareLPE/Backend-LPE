package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.passenger;

import com.example.backend_sistema_LPE.apps.shared.shift.Shift;

public record BoardingShiftWindowResolution(
        Shift shift,
        BoardingShiftWindowType windowType
) {
}
