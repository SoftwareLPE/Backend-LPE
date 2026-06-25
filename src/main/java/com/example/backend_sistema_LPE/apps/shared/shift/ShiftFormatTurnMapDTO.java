package com.example.backend_sistema_LPE.apps.shared.shift;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShiftFormatTurnMapDTO {
    private Long shiftId;
    private String dayOfWeek;
    private String turnName;
}
