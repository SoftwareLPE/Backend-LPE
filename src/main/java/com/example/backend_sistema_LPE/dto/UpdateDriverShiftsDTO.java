package com.example.backend_sistema_LPE.dto;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateDriverShiftsDTO {
    private Set<Long> shiftIds;
}
