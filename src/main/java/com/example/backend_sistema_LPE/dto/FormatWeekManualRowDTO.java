package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FormatWeekManualRowDTO {
    private Long manualRowId;
    private String routeName;
    private String driverName;
    private String driverLastName;
    private String unitType;
    private String secondaryValue;
    private Boolean extraRow;
    private Integer sortOrder;
}
