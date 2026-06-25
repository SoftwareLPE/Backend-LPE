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
public class FlexsurWeekRowDTO {
    private Long flexsurWeekId;
    private Long manualFlexsurRowId;
    private String serviceName;
    private List<FlexsurDetailDTO> details;
    private Integer rowTotal;
}
