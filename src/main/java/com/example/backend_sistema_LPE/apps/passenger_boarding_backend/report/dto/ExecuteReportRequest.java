package com.example.backend_sistema_LPE.apps.passenger_boarding_backend.report.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ExecuteReportRequest {

    @NotNull
    private Long resourceId;

    @NotNull
    private Long templateId;

    @NotNull
    private Long objectId;

    @NotNull
    private Long objectSecId;

    @Min(0)
    private Long intervalFrom;

    @Min(0)
    private Long intervalTo;

    private Integer tableIndex = 0;
    private Integer indexFrom = 0;
    private Integer indexTo = 1000;
    private Boolean forceRefresh = false;
}
