package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegalTripTypeUpdateRequestDTO {
    private String label;
    private Integer sortOrder;
    private Boolean active;
    private java.util.Set<String> dayKeys;
    private String code;
}
