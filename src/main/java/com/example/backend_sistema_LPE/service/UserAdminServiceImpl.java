package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateUserRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateUserRequestDTO;
import com.example.backend_sistema_LPE.dto.UserDetailDTO;
import com.example.backend_sistema_LPE.dto.UserTableDTO;
import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class UserAdminServiceImpl implements UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserCompanyRepository userCompanyRepository;
    private final UserPlantRepository userPlantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAssignmentService userAssignmentService;

    public UserAdminServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserCompanyRepository userCompanyRepository, UserPlantRepository userPlantRepository, PasswordEncoder passwordEncoder, UserAssignmentService userAssignmentService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userCompanyRepository = userCompanyRepository;
        this.userPlantRepository = userPlantRepository;
        this.passwordEncoder = passwordEncoder;
        this.userAssignmentService = userAssignmentService;

    }

    @Override
    public Page<UserTableDTO> getUsersForTable(String q, Long roleId, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createAt").descending()
        );

        return userRepository.findUsersForTable(
                q,
                roleId,
                active,
                pageable
        );

    }

    @Override
    @Transactional
    public UserTableDTO createUser(CreateUserRequestDTO createUserRequestDTO) {
        // 1. Validaciones básicas
        if (userRepository.existsByUserName(createUserRequestDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserRequestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 2. Rol
        Role role = roleRepository.findById(createUserRequestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 3. Crear usuario
        User user = new User();
        user.setName(createUserRequestDTO.getName());
        user.setLastName(createUserRequestDTO.getLastName());
        user.setUserName(createUserRequestDTO.getUsername());
        user.setEmail(createUserRequestDTO.getEmail());
        user.setActive(createUserRequestDTO.getActive());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(createUserRequestDTO.getPassword()));
        user.setCreateAt(new Date());

        user = userRepository.save(user);

        // 4. Asignar compañías
        userAssignmentService.assignCompaniesToUser(
                user.getUserId(),
                createUserRequestDTO.getCompanyIds()
        );

        // 5. Asignar plantas
        if (createUserRequestDTO.getPlantIds() != null && !createUserRequestDTO.getPlantIds().isEmpty()) {
            userAssignmentService.assignPlantsToUser(user.getUserId(), createUserRequestDTO.getPlantIds());
        }

        // 6. Respuesta para tabla
        return new UserTableDTO(
                user.getUserId(),
                user.getName() + " " + user.getLastName(),
                user.getUserName(),
                user.getEmail(),
                user.getActive(),
                role.getRoleId(),
                role.getRoleName()
        );
    }

    @Override
    @Transactional
    public UserTableDTO updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO) {
        // 1. Buscar usuario
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validar email único (si cambia)
        if (updateUserRequestDTO.getEmail() == null || updateUserRequestDTO.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (!java.util.Objects.equals(user.getEmail(), updateUserRequestDTO.getEmail())
                && userRepository.existsByEmail(updateUserRequestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 3. Rol
        Role role = roleRepository.findById(updateUserRequestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // 4. Actualizar datos básicos
        user.setName(updateUserRequestDTO.getName());
        user.setLastName(updateUserRequestDTO.getLastName());
        user.setEmail(updateUserRequestDTO.getEmail());
        user.setActive(updateUserRequestDTO.getActive());
        user.setRole(role);

        userRepository.save(user);

        userAssignmentService.replaceCompaniesForUser(
                userId,
                updateUserRequestDTO.getCompanyIds()
        );

        userAssignmentService.replacePlantsForUser(
                userId,
                updateUserRequestDTO.getPlantIds()
        );

        // 6. Respuesta para tabla
        return new UserTableDTO(
                user.getUserId(),
                user.getName() + " " + user.getLastName(),
                user.getUserName(),
                user.getEmail(),
                user.getActive(),
                role.getRoleId(),
                role.getRoleName()
        );
    }

    @Override
    public UserDetailDTO getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Long> companyIds = userCompanyRepository.findCompanyIdsByUserId(userId);
        List<Long> explicitPlantIds = userPlantRepository.findPlantIdsByUserId(userId);


        List<Long> plantIdsToReturn = explicitPlantIds;

        UserDetailDTO dto = new UserDetailDTO();
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setRoleId(user.getRole().getRoleId());
        dto.setCompanyIds(companyIds);
        dto.setPlantIds(plantIdsToReturn);

        return dto;
    }

    @Override
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        if (currentUserId != null && currentUserId.equals(userId)) {
            throw new RuntimeException("No se puede eliminar el usuario autenticado");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != null && "ADMINISTRADOR".equals(user.getRole().getRoleName())) {
            throw new RuntimeException("No se puede eliminar un usuario ADMINISTRADOR");
        }

        userPlantRepository.deleteByUserUserId(userId);
        userCompanyRepository.deleteByUserUserId(userId);
        userRepository.deleteById(userId);
    }
}




