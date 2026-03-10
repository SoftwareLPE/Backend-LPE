package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatWeekSaveRequestDTO {
    private Long plantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    private Long shiftId;
    private Long formatTypeId;
    private List<FormatWeekManualRowDTO> manualRows;
    private List<FormatWeekRowDTO> rows;
}
