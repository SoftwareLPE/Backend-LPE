package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyListDTO {
    private Long CompanyId;
    private String companyName;
}
