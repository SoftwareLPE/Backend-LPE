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
public class CreateUserRequestDTO {
    // Información personal
    @NotBlank
    private String name;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    // Credenciales
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    // Configuración
    @NotNull
    private Boolean active;

    @NotNull
    private Long roleId;

    @NotEmpty
    private List<Long> companyIds;

    // (opcional ahora, pero ya previsto)
    private List<Long> plantIds;
}
