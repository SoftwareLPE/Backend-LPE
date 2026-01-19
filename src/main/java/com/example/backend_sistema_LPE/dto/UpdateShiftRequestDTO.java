package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;
import java.util.Set;

@Data
public class UpdateShiftRequestDTO {
    private String shiftName;
    private Set<String> dayKeys;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}
