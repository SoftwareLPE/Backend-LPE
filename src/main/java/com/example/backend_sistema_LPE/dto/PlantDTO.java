package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlantDTO {
    private Long plantId;
    private String plantName;
    private Long formatCatalogId;
    private Long formatTypeId;
}
