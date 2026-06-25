package com.example.backend_sistema_LPE.apps.shared.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/permissions")
public class PermissionAdminController {
    private final PermissionService permissionService;

    @GetMapping
    public List<PermissionDTO> getPermissions() {
        return permissionService.getAllPermissions();
    }
}
