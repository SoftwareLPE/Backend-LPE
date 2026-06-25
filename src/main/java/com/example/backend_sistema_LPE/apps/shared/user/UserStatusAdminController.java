package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class UserStatusAdminController {
    private final UserAdminService userAdminService;

    public UserStatusAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PatchMapping("/change-status")
    public ResponseEntity<UserTableDTO> updateUserStatus(
            @RequestBody @Valid UpdateUserActiveDTO request,
            Authentication authentication
    ) {
        Long currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            currentUserId = principal.getUserId();
        }

        UserTableDTO updated = userAdminService.updateUserActive(
                request.getUserId(),
                request.getActive(),
                currentUserId
        );
        return ResponseEntity.ok(updated);
    }
}
