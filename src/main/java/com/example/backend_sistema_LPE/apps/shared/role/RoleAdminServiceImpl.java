package com.example.backend_sistema_LPE.apps.shared.role;

import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyRepository;
import com.example.backend_sistema_LPE.apps.shared.permission.Permission;
import com.example.backend_sistema_LPE.apps.shared.permission.PermissionRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
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
        String normalizedRoleKey = normalizeRoleKey(dto.getRoleKey(), dto.getRoleName());
        String normalizedRoleName = normalizeRequiredValue(dto.getRoleName());

        if (roleRepository.existsByRoleName(normalizedRoleName)) {
            throw new RuntimeException("Role already exists: " + normalizedRoleName);
        }

        if (roleRepository.existsByRoleKey(normalizedRoleKey)) {
            throw new RuntimeException("Role key already exists: " + normalizedRoleKey);
        }

        Role role = new Role();
        role.setRoleKey(normalizedRoleKey);
        role.setRoleName(normalizedRoleName);
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

        return new RoleDTO(savedRole.getRoleId(), savedRole.getRoleKey(), savedRole.getRoleName(), savedRole.getDescription());
    }

    @Transactional
    public RoleDTO updateRole(Long roleId, UpdateRoleRequestDTO dto) {
        String normalizedRoleKey = normalizeRoleKey(dto.getRoleKey(), dto.getRoleName());
        String normalizedRoleName = normalizeRequiredValue(dto.getRoleName());

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (!role.getRoleName().equals(normalizedRoleName) && roleRepository.existsByRoleName(normalizedRoleName)) {
            throw new RuntimeException("Role name already exists: " + normalizedRoleName);
        }

        if (!java.util.Objects.equals(role.getRoleKey(), normalizedRoleKey)
                && roleRepository.existsByRoleKey(normalizedRoleKey)) {
            throw new RuntimeException("Role key already exists: " + normalizedRoleKey);
        }

        role.setRoleKey(normalizedRoleKey);
        role.setRoleName(normalizedRoleName);
        role.setDescription(dto.getDescription());
        roleRepository.save(role);

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

        return new RoleDTO(role.getRoleId(), role.getRoleKey(), role.getRoleName(), role.getDescription());
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
        detail.setRoleKey(role.getRoleKey());
        detail.setRoleName(role.getRoleName());
        detail.setRoleDescription(role.getDescription());
        detail.setPermissionIds(permissionIds);

        return detail;
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        if (!roleRepository.existsById(roleId)) {
            throw new RuntimeException("Role not found");
        }

        rolePermissionRepository.deleteByRoleRoleId(roleId);
        rolePlantRepository.deleteByRoleRoleId(roleId);
        roleCompanyRepository.deleteByRoleRoleId(roleId);

        roleRepository.deleteById(roleId);
    }

    private String normalizeRoleKey(String roleKey, String roleName) {
        String source = roleKey;
        if (source == null || source.trim().isEmpty()) {
            source = roleName;
        }

        String normalized = normalizeRequiredValue(source).toUpperCase();
        normalized = normalized.replaceAll("[^A-Z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        normalized = normalized.replaceAll("_+", "_");

        if (normalized.isEmpty()) {
            throw new RuntimeException("Role key is required");
        }

        return normalized;
    }

    private String normalizeRequiredValue(String value) {
        if (value == null) {
            throw new RuntimeException("Required value is missing");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new RuntimeException("Required value is missing");
        }

        return normalized;
    }
}


