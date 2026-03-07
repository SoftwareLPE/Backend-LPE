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
public class FlexsurDetailDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceDate;
    private Integer trips;
    private Integer extraColumn;
    private Integer total;
}
