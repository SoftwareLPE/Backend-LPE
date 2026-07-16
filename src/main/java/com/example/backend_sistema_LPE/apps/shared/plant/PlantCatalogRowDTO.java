package com.example.backend_sistema_LPE.apps.shared.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlantCatalogRowDTO {
    private Long companyId;
    private String companyName;
    private Long plantId;
    private String plantName;
    private long shiftCount;
    private boolean active;
}
