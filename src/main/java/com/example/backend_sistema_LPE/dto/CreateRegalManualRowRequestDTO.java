package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRegalManualRowRequestDTO {
    private Long plantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;

    private String driverNameOverride;
    private Integer sortOrder;
}
