package com.example.backend_sistema_LPE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestDTO {
    // Datos generales
    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    // Configuración
    @NotNull
    private Boolean active;

    @NotNull
    private Long roleId;

    // Asignaciones
    @NotEmpty
    private List<Long> companyIds;

    @NotEmpty
    private List<Long> plantIds;
}

