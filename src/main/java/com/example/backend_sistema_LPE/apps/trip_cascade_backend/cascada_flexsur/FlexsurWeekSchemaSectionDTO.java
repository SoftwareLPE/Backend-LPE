package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlexsurWeekSchemaSectionDTO {
    private String key;
    private List<FlexsurWeekSchemaDayDTO> days;
}
