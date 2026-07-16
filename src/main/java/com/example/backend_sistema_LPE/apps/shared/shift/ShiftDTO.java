package com.example.backend_sistema_LPE.apps.shared.shift;

import com.example.backend_sistema_LPE.apps.shared.enums.ShiftType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class ShiftDTO {
    private Long shiftId;
    private String shiftName;
    private Boolean active;
    private Set<String> wialonAliasNames = new HashSet<>();
    private ShiftType shiftType;
    private Set<String> dayKeys;
    private Set<String> longWeekDayKeys;
    private Set<String> shortWeekDayKeys;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
