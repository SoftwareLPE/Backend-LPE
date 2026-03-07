package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalDetailDTO {
    private Long tripTypeId;
    private String tripTypeCode;
    private String dayOfWeek;
    private Integer tripCount;
}
