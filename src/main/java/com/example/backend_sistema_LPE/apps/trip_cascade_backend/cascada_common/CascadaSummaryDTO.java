package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
public class CascadaSummaryDTO {

    private Long cascadaId;
    private String id;
    private Long plantId;
    private String plantName;
    private Long companyId;
    private String companyName;

    private String sentBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
    private Integer weekNumber;

    private Set<String> shiftIds;

    private Set<String> dayKeys;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime currentVersion;

    private String visualStatus;

    public CascadaSummaryDTO(
            Long cascadaId,
            String id,
            Long plantId,
            String plantName,
            Long companyId,
            String companyName,
            String sentBy,
            LocalDate weekDate,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            Set<String> shiftIds,
            Set<String> dayKeys,
            LocalDateTime sentAt,
            LocalDateTime updatedAt
    ) {
        this.cascadaId = cascadaId;
        this.id = id;
        this.plantId = plantId;
        this.plantName = plantName;
        this.companyId = companyId;
        this.companyName = companyName;
        this.sentBy = sentBy;
        this.weekDate = weekDate;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.weekNumber = weekNumber;
        this.shiftIds = shiftIds;
        this.dayKeys = dayKeys;
        this.sentAt = sentAt;
        this.updatedAt = updatedAt;
    }
}
