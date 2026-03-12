package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateFlexsurServiceDriverAssignmentRequestDTO {
    private Long driverId;
    private Boolean active;
}
