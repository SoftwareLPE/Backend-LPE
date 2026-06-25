package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegalTripTypeCreateRequestDTO {
    private String label;
    private Integer sortOrder;
    private Boolean active;
    private java.util.Set<String> dayKeys;
    private String code;
}
