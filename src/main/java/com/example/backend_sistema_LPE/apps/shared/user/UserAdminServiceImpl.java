package com.example.backend_sistema_LPE.apps.shared.user;

import com.example.backend_sistema_LPE.apps.shared.role.Role;
import com.example.backend_sistema_LPE.apps.shared.role.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public Page<UserAdminTableRowDTO> getUsersForTable(String q, Long roleId, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createAt").descending()
        );

        Page<UserTableDTO> basePage = userRepository.findUsersForTable(
                q,
                roleId,
                active,
                pageable
        );

        List<Long> userIds = basePage.getContent().stream()
                .map(UserTableDTO::getUserId)
                .toList();

        if (userIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, basePage.getTotalElements());
        }

        Map<Long, AssignmentAggregate> assignmentsByUserId = buildAssignmentsByUserId(userIds);

        List<UserAdminTableRowDTO> enrichedRows = basePage.getContent().stream()
                .map(row -> {
                    AssignmentAggregate aggregate = assignmentsByUserId.getOrDefault(
                            row.getUserId(),
                            AssignmentAggregate.empty()
                    );

                    return new UserAdminTableRowDTO(
                            row.getUserId(),
                            row.getFullname(),
                            row.getUserName(),
                            row.getEmail(),
                            row.getActive(),
                            row.getRoleId(),
                            row.getRoleName(),
                            aggregate.companyIds(),
                            aggregate.companyNames(),
                            aggregate.plantIds(),
                            aggregate.plantNames()
                    );
                })
                .toList();

        return new PageImpl<>(enrichedRows, pageable, basePage.getTotalElements());
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
        String normalizedPassword = resolveUpdatedPassword(updateUserRequestDTO);

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

    @Override
    @Transactional
    public UserTableDTO updateUserActive(Long userId, Boolean active, Long currentUserId) {
        if (active == null) {
            throw new RuntimeException("Active is required");
        }

        if (currentUserId != null && currentUserId.equals(userId) && !active) {
            throw new RuntimeException("No se puede desactivar el usuario autenticado");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(active);
        userRepository.save(user);

        Role role = user.getRole();
        return new UserTableDTO(
                user.getUserId(),
                user.getName() + " " + user.getLastName(),
                user.getUserName(),
                user.getEmail(),
                user.getActive(),
                role != null ? role.getRoleId() : null,
                role != null ? role.getRoleName() : null
        );
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

    private String resolveUpdatedPassword(UpdateUserRequestDTO request) {
        String normalizedUserPassword = normalizeOptionalPassword(request.getUserPassword());
        if (normalizedUserPassword != null) {
            return normalizedUserPassword;
        }
        return normalizeOptionalPassword(request.getPassword());
    }

    private Map<Long, AssignmentAggregate> buildAssignmentsByUserId(List<Long> userIds) {
        Map<Long, AssignmentAggregateBuilder> builders = new LinkedHashMap<>();
        for (Long userId : userIds) {
            builders.put(userId, new AssignmentAggregateBuilder());
        }

        List<UserCompanyAssignmentRowDTO> companyAssignments =
                userCompanyRepository.findCompanyAssignmentsByUserIds(userIds);
        for (UserCompanyAssignmentRowDTO row : companyAssignments) {
            AssignmentAggregateBuilder builder = builders.computeIfAbsent(
                    row.getUserId(),
                    ignored -> new AssignmentAggregateBuilder()
            );
            builder.companyIds.add(row.getCompanyId());
            builder.companyNames.add(row.getCompanyName());
        }

        List<UserPlantAssignmentRowDTO> plantAssignments =
                userPlantRepository.findPlantAssignmentsByUserIds(userIds);
        for (UserPlantAssignmentRowDTO row : plantAssignments) {
            AssignmentAggregateBuilder builder = builders.computeIfAbsent(
                    row.getUserId(),
                    ignored -> new AssignmentAggregateBuilder()
            );
            builder.plantIds.add(row.getPlantId());
            builder.plantNames.add(row.getPlantName());
        }

        Map<Long, AssignmentAggregate> result = new LinkedHashMap<>();
        builders.forEach((userId, builder) -> result.put(userId, builder.build()));
        return result;
    }

    private record AssignmentAggregate(
            List<Long> companyIds,
            List<String> companyNames,
            List<Long> plantIds,
            List<String> plantNames
    ) {
        private static AssignmentAggregate empty() {
            return new AssignmentAggregate(List.of(), List.of(), List.of(), List.of());
        }
    }

    private static class AssignmentAggregateBuilder {
        private final List<Long> companyIds = new java.util.ArrayList<>();
        private final List<String> companyNames = new java.util.ArrayList<>();
        private final List<Long> plantIds = new java.util.ArrayList<>();
        private final List<String> plantNames = new java.util.ArrayList<>();

        private AssignmentAggregate build() {
            return new AssignmentAggregate(
                    List.copyOf(companyIds),
                    List.copyOf(companyNames),
                    List.copyOf(plantIds),
                    List.copyOf(plantNames)
            );
        }
    }
}
