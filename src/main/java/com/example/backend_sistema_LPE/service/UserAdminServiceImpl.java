package com.example.backend_sistema_LPE.service;

import com.example.backend_sistema_LPE.dto.CreateUserRequestDTO;
import com.example.backend_sistema_LPE.dto.UpdateUserRequestDTO;
import com.example.backend_sistema_LPE.dto.UserDetailDTO;
import com.example.backend_sistema_LPE.dto.UserTableDTO;
import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.RoleRepository;
import com.example.backend_sistema_LPE.repository.UserCompanyRepository;
import com.example.backend_sistema_LPE.repository.UserPlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
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

    public UserAdminServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserCompanyRepository userCompanyRepository,
            UserPlantRepository userPlantRepository,
            PasswordEncoder passwordEncoder,
            UserAssignmentService userAssignmentService
    ) {
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
        String normalizedEmail = normalizeOptionalEmail(createUserRequestDTO.getEmail());

        if (userRepository.existsByUserName(createUserRequestDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findById(createUserRequestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(createUserRequestDTO.getName());
        user.setLastName(createUserRequestDTO.getLastName());
        user.setUserName(createUserRequestDTO.getUsername());
        user.setEmail(normalizedEmail);
        user.setActive(createUserRequestDTO.getActive());
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(createUserRequestDTO.getPassword()));
        user.setCreateAt(new Date());

        user = userRepository.save(user);

        userAssignmentService.assignCompaniesToUser(
                user.getUserId(),
                createUserRequestDTO.getCompanyIds()
        );

        if (createUserRequestDTO.getPlantIds() != null && !createUserRequestDTO.getPlantIds().isEmpty()) {
            userAssignmentService.assignPlantsToUser(user.getUserId(), createUserRequestDTO.getPlantIds());
        }

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
        String normalizedEmail = normalizeOptionalEmail(updateUserRequestDTO.getEmail());
        String normalizedUserName = normalizeRequiredValue(updateUserRequestDTO.getUserName());
        String normalizedPassword = normalizeOptionalPassword(updateUserRequestDTO.getPassword());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!java.util.Objects.equals(user.getUserName(), normalizedUserName)
                && userRepository.existsByUserName(normalizedUserName)) {
            throw new RuntimeException("Username already exists");
        }

        if (normalizedEmail != null
                && !java.util.Objects.equals(user.getEmail(), normalizedEmail)
                && userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository.findById(updateUserRequestDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setName(updateUserRequestDTO.getName());
        user.setLastName(updateUserRequestDTO.getLastName());
        user.setUserName(normalizedUserName);
        user.setEmail(normalizedEmail);
        user.setActive(updateUserRequestDTO.getActive());
        user.setRole(role);
        if (normalizedPassword != null) {
            user.setPassword(passwordEncoder.encode(normalizedPassword));
        }

        userRepository.save(user);

        userAssignmentService.replaceCompaniesForUser(
                userId,
                updateUserRequestDTO.getCompanyIds()
        );

        userAssignmentService.replacePlantsForUser(
                userId,
                updateUserRequestDTO.getPlantIds()
        );

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

        UserDetailDTO dto = new UserDetailDTO();
        dto.setName(user.getName());
        dto.setLastName(user.getLastName());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setActive(user.getActive());
        dto.setRoleId(user.getRole().getRoleId());
        dto.setCompanyIds(companyIds);
        dto.setPlantIds(explicitPlantIds);

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

    private String normalizeOptionalEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeRequiredValue(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeOptionalPassword(String password) {
        if (password == null) {
            return null;
        }

        String normalized = password.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
