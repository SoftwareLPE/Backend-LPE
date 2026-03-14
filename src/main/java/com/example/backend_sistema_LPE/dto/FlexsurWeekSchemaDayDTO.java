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
public class FlexsurWeekSchemaDayDTO {
    private String dayKey;
    private String dayLabel;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceDate;
    private List<FlexsurWeekSchemaColumnDTO> columns;
}
