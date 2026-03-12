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
public class CreateFormatWeekManualRowRequestDTO {
    private Long plantId;
    private Long formatTypeId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;

    private String routeName;
    private String driverName;
    private String driverLastName;
    private String unitType;
    private String secondaryValue;
    private Boolean extraRow;
    private Integer sortOrder;
}
