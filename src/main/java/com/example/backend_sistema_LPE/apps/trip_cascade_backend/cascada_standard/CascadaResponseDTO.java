package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CascadaResponseDTO {
    private Long plantId;
    private String shiftId;
    private String dayKey;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
    private Integer weekNumber;

    private List<CascadaStandardManualRowDTO> manualRows;
    private List<CascadaRowDTO> rows;
}
