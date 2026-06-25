package com.example.backend_sistema_LPE.apps.trip_cascade_backend.company;

import com.example.backend_sistema_LPE.apps.shared.plant.CreateRequestPlantDTO;
import lombok.Data;

import java.util.List;

@Data
public class CreateCompanyRequestDTO {
    String companyName;
    List<CreateRequestPlantDTO> plants;
}
