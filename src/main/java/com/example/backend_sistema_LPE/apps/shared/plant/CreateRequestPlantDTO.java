package com.example.backend_sistema_LPE.apps.shared.plant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRequestPlantDTO {
    String plantName;
    Long formatCatalogId;
    Long formatTypeId;
}
