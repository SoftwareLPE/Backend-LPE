package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_standard;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverViewDTO;
import com.example.backend_sistema_LPE.apps.shared.shift.ShiftDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CascadaInboxViewDTO {
    private Long plantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;

    private String status;
    private List<ShiftDTO> shifts;
    private List<DriverViewDTO> drivers;
    private List<CascadaWeekItemDTO> items;
}
