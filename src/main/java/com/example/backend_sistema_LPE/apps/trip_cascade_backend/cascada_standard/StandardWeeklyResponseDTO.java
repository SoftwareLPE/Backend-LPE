package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

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
public class StandardWeeklyResponseDTO {
    private Long plantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    private String status;
    private List<StandardWeeklyShiftDTO> shifts;
}
