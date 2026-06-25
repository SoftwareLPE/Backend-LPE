package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

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
public class RegalWeekResponseDTO {
    private Long plantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    private Long shiftId;
    private List<RegalWeekRowDTO> rows;
    private RegalWeekTotalsDTO totals;
}
