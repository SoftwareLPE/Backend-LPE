package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.RoleSummaryDTO;
import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class RoleAdminSummaryServiceImpl implements RoleAdminSummaryService{
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RoleAdminSummaryServiceImpl(RoleRepository roleRepository, RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public List<RoleSummaryDTO> getRolesSummary() {

        // 1) Roles base
        List<Role> roles = roleRepository.findAll();

        // 2) permissionsCount por rol
        Map<Long, Long> permissionsCountByRole = new HashMap<>();
        for (Object[] row : rolePermissionRepository.countPermissionsByRole()) {
            Long roleId = (Long) row[0];
            Long count = (Long) row[1];
            permissionsCountByRole.put(roleId, count);
        }

        // 3) Construir summary por rol
        return roles.stream()
                .map(role -> {
                    Long roleId = role.getRoleId();
                    long permissionsCount = permissionsCountByRole.getOrDefault(roleId, 0L);

                    return new RoleSummaryDTO(
                            role.getRoleId(),
                            role.getRoleKey(),
                            role.getRoleName(),
                            role.getDescription(),
                            permissionsCount
                    );
                })
                // opcional: ordenar por nombre
                .sorted(java.util.Comparator.comparing(
                        RoleSummaryDTO::getRoleName,
                        java.util.Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .collect(Collectors.toList());
    }

}




