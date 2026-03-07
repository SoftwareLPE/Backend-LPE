package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlexsurServiceDTO {
    private Long serviceId;
    private Long plantId;
    private String serviceName;
    private Integer sortOrder;
    private Boolean active;
}
