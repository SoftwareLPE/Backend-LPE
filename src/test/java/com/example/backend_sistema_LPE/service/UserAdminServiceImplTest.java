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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserCompanyRepository userCompanyRepository;
    @Mock
    private UserPlantRepository userPlantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserAssignmentService userAssignmentService;

    private UserAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserAdminServiceImpl(
                userRepository,
                roleRepository,
                userCompanyRepository,
                userPlantRepository,
                passwordEncoder,
                userAssignmentService
        );
    }

    @Test
    void createUser_rejectsDuplicateUsername() {
        CreateUserRequestDTO request = baseCreateRequest();

        when(userRepository.existsByUserName("user")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_rejectsDuplicateEmail() {
        CreateUserRequestDTO request = baseCreateRequest();

        when(userRepository.existsByUserName("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_assignsCompaniesAndPlants() {
        CreateUserRequestDTO request = baseCreateRequest();
        request.setCompanyIds(List.of(1L, 2L));
        request.setPlantIds(List.of(5L));

        Role role = new Role();
        role.setRoleId(3L);
        role.setRoleName("COORDINADOR");

        when(userRepository.existsByUserName("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("secret")).thenReturn("HASHED");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User saved = invocation.getArgument(0);
                    saved.setUserId(10L);
                    return saved;
                });

        UserTableDTO result = service.createUser(request);

        assertThat(result.getUserId()).isEqualTo(10L);
        verify(userAssignmentService).assignCompaniesToUser(10L, List.of(1L, 2L));
        verify(userAssignmentService).assignPlantsToUser(10L, List.of(5L));
    }

    @Test
    void updateUser_rejectsMissingEmail() {
        UpdateUserRequestDTO request = baseUpdateRequest();
        request.setEmail(" ");

        when(userRepository.findById(10L)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> service.updateUser(10L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    void updateUser_rejectsDuplicateEmail() {
        UpdateUserRequestDTO request = baseUpdateRequest();

        User existing = new User();
        existing.setEmail("old@example.com");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.updateUser(10L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateUser_updatesAssignments() {
        UpdateUserRequestDTO request = baseUpdateRequest();
        request.setCompanyIds(List.of(1L));
        request.setPlantIds(List.of(2L));

        User existing = new User();
        existing.setUserId(10L);
        existing.setEmail("old@example.com");

        Role role = new Role();
        role.setRoleId(3L);
        role.setRoleName("COORDINADOR");

        when(userRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role));

        UserTableDTO result = service.updateUser(10L, request);

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        verify(userAssignmentService).replaceCompaniesForUser(10L, List.of(1L));
        verify(userAssignmentService).replacePlantsForUser(10L, List.of(2L));
    }

    @Test
    void getUserDetail_returnsCompanyAndPlantIds() {
        User user = new User();
        user.setUserId(10L);
        user.setName("Ana");
        user.setLastName("Lopez");
        user.setUserName("alopez");
        user.setEmail("ana@example.com");
        user.setActive(true);
        Role role = new Role();
        role.setRoleId(5L);
        user.setRole(role);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userCompanyRepository.findCompanyIdsByUserId(10L)).thenReturn(List.of(1L, 2L));
        when(userPlantRepository.findPlantIdsByUserId(10L)).thenReturn(List.of(7L));

        UserDetailDTO detail = service.getUserDetail(10L);

        assertThat(detail.getCompanyIds()).containsExactly(1L, 2L);
        assertThat(detail.getPlantIds()).containsExactly(7L);
        assertThat(detail.getRoleId()).isEqualTo(5L);
    }

    @Test
    void deleteUser_rejectsSelfDelete() {
        assertThatThrownBy(() -> service.deleteUser(10L, 10L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede eliminar el usuario autenticado");
    }

    @Test
    void deleteUser_rejectsAdminRole() {
        Role role = new Role();
        role.setRoleName("ADMINISTRADOR");
        User user = new User();
        user.setRole(role);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.deleteUser(10L, 5L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No se puede eliminar un usuario ADMINISTRADOR");
    }

    @Test
    void deleteUser_removesAssignmentsAndUser() {
        Role role = new Role();
        role.setRoleName("COORDINADOR");
        User user = new User();
        user.setRole(role);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        service.deleteUser(10L, 5L);

        verify(userPlantRepository).deleteByUserUserId(10L);
        verify(userCompanyRepository).deleteByUserUserId(10L);
        verify(userRepository).deleteById(10L);
    }

    private CreateUserRequestDTO baseCreateRequest() {
        CreateUserRequestDTO request = new CreateUserRequestDTO();
        request.setName("Juan");
        request.setLastName("Perez");
        request.setEmail("user@example.com");
        request.setUsername("user");
        request.setPassword("secret");
        request.setActive(true);
        request.setRoleId(3L);
        request.setCompanyIds(List.of(1L));
        return request;
    }

    private UpdateUserRequestDTO baseUpdateRequest() {
        UpdateUserRequestDTO request = new UpdateUserRequestDTO();
        request.setName("Juan");
        request.setLastName("Perez");
        request.setEmail("new@example.com");
        request.setActive(true);
        request.setRoleId(3L);
        request.setCompanyIds(List.of(1L));
        request.setPlantIds(List.of(2L));
        return request;
    }
}
