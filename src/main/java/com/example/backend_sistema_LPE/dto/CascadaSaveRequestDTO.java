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

    private Map<String, List<CascadaRowDTO>> days;
}
