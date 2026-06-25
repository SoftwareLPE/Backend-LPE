package com.example.backend_sistema_LPE.apps.shared.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlantFormatDTO {
    private Long plantId;
    private Long formatCatalogId;
    private Long formatTypeId;
}
