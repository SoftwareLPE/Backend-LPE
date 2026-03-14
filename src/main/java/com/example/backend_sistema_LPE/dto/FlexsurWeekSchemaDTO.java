package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlexsurWeekSchemaDTO {
    private Long plantId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    private List<String> baseColumns;
    private List<FlexsurWeekSchemaDayDTO> days;
    private List<FlexsurWeekSchemaSectionDTO> sections;
    private String weeklyTotalColumn;
}
