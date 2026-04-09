package com.example.backend_sistema_LPE.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
public class CascadaSummaryDTO {
    // Group key for the sent cascada bucket
    private Long cascadaId;
    private String id;
    private Long plantId;
    private String plantName;
    private Long companyId;
    private String companyName;
    // Display name of the user who sent the cascada
    private String sentBy;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekEndDate;
    private Integer weekNumber;
    // All shift ids included for the week.
    private Set<String> shiftIds;
    // All day keys included for the week.
    private Set<String> dayKeys;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;
}
