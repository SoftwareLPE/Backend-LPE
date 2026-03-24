package com.example.backend_sistema_LPE.dto;

import com.example.backend_sistema_LPE.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;
import java.util.Set;

@Data
public class ShiftDTO {
    private Long shiftId;
    private String shiftName;
    private ShiftType shiftType;
    private Set<String> dayKeys;
    private Set<String> longWeekDayKeys;
    private Set<String> shortWeekDayKeys;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
