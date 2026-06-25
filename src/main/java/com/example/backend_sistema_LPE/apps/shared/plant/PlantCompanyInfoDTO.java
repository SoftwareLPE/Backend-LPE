package com.example.backend_sistema_LPE.apps.shared.plant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlantCompanyInfoDTO {
    private Long plantId;
    private String plantName;
    private Long companyId;
    private String companyName;
}
