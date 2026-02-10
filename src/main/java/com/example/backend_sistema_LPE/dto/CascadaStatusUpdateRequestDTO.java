package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CascadaStatusUpdateRequestDTO {
    private LocalDate weekDate;
    private String shiftId;
    private String dayKey;
    private String status;
    private List<Long> recipientUserIds;
}
