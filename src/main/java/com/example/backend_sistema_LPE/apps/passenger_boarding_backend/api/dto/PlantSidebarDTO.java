package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlantSidebarDTO {
    private Long plantId;
    private String plantName;
    private Long unitsCount;
    private Long companyId;
    private String companyName;
}
