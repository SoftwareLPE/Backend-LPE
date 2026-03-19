package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CascadaSaveRequestDTO {
    private Long plantId;
    private String shiftId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
    private Integer weekNumber;

    private String dayKey;
    private List<CascadaStandardManualRowDTO> manualRows;
    private List<CascadaRowDTO> rows;
    private Map<String, List<CascadaRowDTO>> days;
}
