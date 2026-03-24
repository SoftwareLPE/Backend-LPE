package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class FlexsurServiceDTO {
    private Long serviceId;
    private Long plantId;
    private String serviceName;
    private String serviceType;
    private Long shiftId;
    private String shiftName;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime serviceTime;
    private String specialWeekType;
    private Integer sortOrder;
    private Boolean active;
}
