package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RegalTripTypeDTO {
    private Long tripTypeId;
    private String code;
    private String label;
    private Integer sortOrder;
    private Boolean active;
    private java.util.Set<String> dayKeys;
}
