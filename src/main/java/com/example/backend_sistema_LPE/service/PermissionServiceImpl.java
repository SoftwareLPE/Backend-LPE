package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.PermissionDTO;
import com.example.backend_sistema_LPE.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

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
