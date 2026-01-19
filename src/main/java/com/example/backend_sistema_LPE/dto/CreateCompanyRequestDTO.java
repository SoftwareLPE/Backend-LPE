package com.example.backend_sistema_LPE.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateCompanyRequestDTO {
    String companyName;
    List<CreateRequestPlantDTO> plants;
}
