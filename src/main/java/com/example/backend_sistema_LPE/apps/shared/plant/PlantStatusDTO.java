package com.example.backend_sistema_LPE.apps.shared.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantStatusDTO {
    private Long plantId;
    private Boolean active;
}
