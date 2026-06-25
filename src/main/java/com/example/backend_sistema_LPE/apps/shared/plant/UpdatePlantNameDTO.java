package com.example.backend_sistema_LPE.apps.shared.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePlantNameDTO {
    private String plantName;
    private Long formatCatalogId;
    private Long formatTypeId;
}
