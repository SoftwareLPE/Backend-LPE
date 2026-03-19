package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class CascadaStatusUpdateRequestDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
    private Integer weekNumber;
    private String shiftId;
    private String dayKey;
    private String status;
    private List<Long> recipientUserIds;
    private Long formatTypeId;
}
