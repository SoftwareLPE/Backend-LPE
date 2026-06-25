package com.example.backend_sistema_LPE.apps.shared.shift;

import com.example.backend_sistema_LPE.apps.shared.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;
import java.util.Set;

@Data
public class UpdateShiftRequestDTO {
    private String shiftName;
    private Set<String> wialonAliasNames;
    private ShiftType shiftType;
    private Set<String> dayKeys;
    private Set<String> longWeekDayKeys;
    private Set<String> shortWeekDayKeys;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
