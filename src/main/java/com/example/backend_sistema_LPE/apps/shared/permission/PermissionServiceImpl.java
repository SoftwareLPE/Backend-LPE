package com.example.backend_sistema_LPE.apps.shared.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService{
    private final PermissionRepository permissionRepository;

    @Override
    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(p -> new PermissionDTO(
                        p.getPermissionId(),
                        p.getCode(),
                        p.getDescription()
                ))
                .toList();
    }
}
