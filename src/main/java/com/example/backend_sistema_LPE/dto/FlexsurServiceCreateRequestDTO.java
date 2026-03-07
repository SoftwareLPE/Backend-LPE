package com.example.backend_sistema_LPE.dto;

import lombok.Data;

@Data
public class FlexsurServiceCreateRequestDTO {
    private Long plantId;
    private String serviceName;
    private Integer sortOrder;
}
