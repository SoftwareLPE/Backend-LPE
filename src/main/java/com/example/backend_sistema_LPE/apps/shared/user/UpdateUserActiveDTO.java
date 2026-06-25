package com.example.backend_sistema_LPE.apps.shared.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserActiveDTO {
    @NotNull
    private Long userId;

    @NotNull
    private Boolean active;
}
