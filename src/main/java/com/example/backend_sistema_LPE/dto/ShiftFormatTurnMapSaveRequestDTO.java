package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShiftFormatTurnMapSaveRequestDTO {
    private Long plantId;
    private Long formatTypeId;
    private List<ShiftFormatTurnMapDTO> mappings;
}
