package com.example.backend_sistema_LPE.dto;

import lombok.Data;

@Data
public class FlexsurServiceUpdateRequestDTO {
    private String serviceName;
    private Integer sortOrder;
    private Boolean active;
}
