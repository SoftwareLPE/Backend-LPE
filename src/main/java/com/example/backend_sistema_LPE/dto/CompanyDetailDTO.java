package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDetailDTO {
    private Long companyId;
    private String companyName;
    List<PlantDTO> plantDTOList;
}
