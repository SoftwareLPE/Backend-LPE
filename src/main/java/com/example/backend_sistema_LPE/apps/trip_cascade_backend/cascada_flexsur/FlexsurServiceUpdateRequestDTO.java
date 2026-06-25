package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_flexsur;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalTime;

@Data
public class FlexsurServiceUpdateRequestDTO {
    private String serviceName;
    private String serviceType;
    private Long shiftId;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime serviceTime;
    private String specialWeekType;
    private Integer sortOrder;
    private Boolean active;
}
