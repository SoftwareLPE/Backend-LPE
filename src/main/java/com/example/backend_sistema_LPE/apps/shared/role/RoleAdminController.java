package com.example.backend_sistema_LPE.apps.shared.role;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
public class RoleAdminController {
    private final RoleAdminService roleAdminService;

    @PostMapping
    public ResponseEntity<RoleDTO> createRole(@RequestBody @Valid CreateRoleRequestDTO dto) {
        return ResponseEntity.ok(roleAdminService.createRole(dto));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable Long roleId,
            @RequestBody @Valid UpdateRoleRequestDTO dto
    ) {
        RoleDTO updated = roleAdminService.updateRole(roleId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDetailDTO> getRoleDetail(
            @PathVariable Long roleId
    ) {
        return ResponseEntity.ok(roleAdminService.getRoleDetail(roleId));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleAdminService.deleteRole(roleId);
        return ResponseEntity.noContent().build(); // 204
    }
}
