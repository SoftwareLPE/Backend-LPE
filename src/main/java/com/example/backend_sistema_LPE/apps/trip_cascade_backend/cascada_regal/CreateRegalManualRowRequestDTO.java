package com.example.backend_sistema_LPE.apps.trip_cascade_backend.cascada_regal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRegalManualRowRequestDTO {
    private Long plantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate weekDate;

    private String driverNameOverride;
    private String routeName;
    @JsonAlias("recorrido")
    private String routeLocation;
    private Integer sortOrder;
}
