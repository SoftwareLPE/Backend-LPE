package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.*;
import com.example.backend_sistema_LPE.model.*;
import com.example.backend_sistema_LPE.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleAdminServiceImpl implements RoleAdminService{

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    private final CompanyRepository companyRepository;
    private final PlantRepository plantRepository;

    private final RoleCompanyRepository roleCompanyRepository;
    private final RolePlantRepository rolePlantRepository;

    public RoleAdminServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository, RolePermissionRepository rolePermissionRepository, CompanyRepository companyRepository, PlantRepository plantRepository, RoleCompanyRepository roleCompanyRepository, RolePlantRepository rolePlantRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.companyRepository = companyRepository;
        this.plantRepository = plantRepository;
        this.roleCompanyRepository = roleCompanyRepository;
        this.rolePlantRepository = rolePlantRepository;
    }


    @Transactional
    public RoleDTO createRole(CreateRoleRequestDTO dto) {

        if (roleRepository.existsByRoleName(dto.getRoleName())) {
            throw new RuntimeException("Role already exists: " + dto.getRoleName());
        }

        Role role = new Role();
        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());
        Role savedRole = roleRepository.save(role);

        // Permisos (deduplicar para evitar inserts duplicados)
        Set<Long> uniquePermissionIds = new HashSet<>(dto.getPermissionIds());
        List<Permission> permissions = permissionRepository.findAllById(uniquePermissionIds);
        if (permissions.size() != uniquePermissionIds.size()) {
            throw new RuntimeException("One or more permissionIds are invalid");
        }

        for (Permission p : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRole(savedRole);
            rp.setPermission(p);
            rp.setCreatedAt(LocalDateTime.now());
            rolePermissionRepository.save(rp);
        }

        return new RoleDTO(savedRole.getRoleId(), savedRole.getRoleName(), savedRole.getDescription());
    }

    @Transactional
    public RoleDTO updateRole(Long roleId, UpdateRoleRequestDTO dto) {

        // 1) Buscar rol
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 2) Validar roleName  (si cambia)
        String newName = dto.getRoleName();
        if (!role.getRoleName().equals(newName) && roleRepository.existsByRoleName(newName)) {
            throw new RuntimeException("Role name already exists: " + newName);
        }

        // 3) Actualizar campos basicos
        role.setRoleName(newName);
        role.setDescription(dto.getDescription());
        roleRepository.save(role);

        // 4) Reemplazar permisos (DEDUP)
        rolePermissionRepository.deleteByRoleRoleId(roleId);
        rolePermissionRepository.flush();

        if (dto.getPermissionIds() == null || dto.getPermissionIds().isEmpty()) {
            throw new RuntimeException("permissionIds must not be empty");
        }

        Set<Long> uniquePermissionIds = new HashSet<>(dto.getPermissionIds());

        List<Permission> permissions = permissionRepository.findAllById(uniquePermissionIds);
        if (permissions.size() != uniquePermissionIds.size()) {
            throw new RuntimeException("One or more permissionIds are invalid");
        }

        for (Permission p : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermission(p);
            rp.setCreatedAt(LocalDateTime.now());
            rolePermissionRepository.save(rp);
        }

        return new RoleDTO(role.getRoleId(), role.getRoleName(), role.getDescription());
    }

    @Override
    @Transactional
    public RoleDetailDTO getRoleDetail(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Long> permissionIds =
                rolePermissionRepository.findPermissionIdsByRoleId(roleId);

        RoleDetailDTO detail = new RoleDetailDTO();
        detail.setRoleId(role.getRoleId());
        detail.setRoleName(role.getRoleName());
        detail.setRoleDescription(role.getDescription());
        detail.setPermissionIds(permissionIds);

        return detail;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        // 1) Verificar que el rol exista
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found");
        }

        // 2) Eliminar relaciones (orden importante)
        rolePermissionRepository.deleteByRoleRoleId(roleId);
        rolePlantRepository.deleteByRoleRoleId(roleId);
        roleCompanyRepository.deleteByRoleRoleId(roleId);

        // 3) Eliminar rol
        roleRepository.deleteById(roleId);
    }
}


