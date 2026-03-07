package com.example.backend_sistema_LPE.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRouteRequestDTO {
    private Long plantId;
    private String routeName;
    private String location;
}
