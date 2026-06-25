package com.example.backend_sistema_LPE.apps.shared.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRecipientDTO {
    private Long userId;
    private String name;
    private String lastName;
    private String userName;
    private String email;
}
