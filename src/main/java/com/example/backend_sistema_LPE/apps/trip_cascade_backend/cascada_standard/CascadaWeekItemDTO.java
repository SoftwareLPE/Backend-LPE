package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CascadaWeekItemDTO {
    private Long shiftId;
    private String dayKey;
    private List<CascadaRowDTO> rows;
}
