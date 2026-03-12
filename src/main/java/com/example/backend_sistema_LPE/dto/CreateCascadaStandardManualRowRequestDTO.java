package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCascadaStandardManualRowRequestDTO {
    private Long plantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;

    private String driverNameOverride;
    private Long routeId;
    private String routeName;
    private Integer sortOrder;
}
