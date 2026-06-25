package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PlantSyncRequest {
    private Long intervalFrom;
    private Long intervalTo;
    private Boolean forceRefresh = false;
}
