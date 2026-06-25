package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users-rol")
public class UserAdminController {
    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @PostMapping
    public UserTableDTO createUser(@RequestBody @Valid CreateUserRequestDTO CreateUserRequestDTO) {
        return userAdminService.createUser(CreateUserRequestDTO);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserTableDTO> updateUser(
            @PathVariable Long userId,
            @RequestBody @Valid UpdateUserRequestDTO updateUserRequestDTO
    ) {
        UserTableDTO updated = userAdminService.updateUser(userId, updateUserRequestDTO);
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public Page<UserAdminTableRowDTO> getUsersForTable(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return userAdminService.getUsersForTable(q, roleId, active, page, size);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDetailDTO> getUserDetail(@PathVariable Long userId) {
        UserDetailDTO dto = userAdminService.getUserDetail(userId);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId, Authentication authentication) {
        Long currentUserId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            currentUserId = principal.getUserId();
        }
        userAdminService.deleteUser(userId, currentUserId);
        return ResponseEntity.noContent().build();
    }

}
