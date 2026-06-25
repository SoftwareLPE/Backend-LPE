package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.apps.shared.auth.AuthController;
import com.example.backend_sistema_LPE.apps.shared.auth.AuthRequest;
import com.example.backend_sistema_LPE.apps.shared.auth.AuthResponse;
import com.example.backend_sistema_LPE.apps.shared.auth.RegisterRequest;
import com.example.backend_sistema_LPE.apps.shared.permission.Permission;
import com.example.backend_sistema_LPE.apps.shared.role.Role;
import com.example.backend_sistema_LPE.apps.shared.user.User;
import com.example.backend_sistema_LPE.apps.shared.permission.PermissionRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RolePermissionRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RoleRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserPlantRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserRepository;
import com.example.backend_sistema_LPE.apps.shared.security.JwtConfig;
import com.example.backend_sistema_LPE.apps.shared.security.MyUserDetailsService;
import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtConfig jwtConfig;
    @Mock
    private MyUserDetailsService userDetailsService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPlantRepository userPlantRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                authenticationManager,
                jwtConfig,
                userDetailsService,
                userRepository,
                userPlantRepository,
                passwordEncoder,
                roleRepository,
                rolePermissionRepository,
                permissionRepository
        );
    }

    @Test
    void login_returnsTokenForValidUser() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("secret");

        UserPrincipal principal = new UserPrincipal(
                10L,
                "admin",
                "secret",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
        );

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtConfig.generateToken(principal)).thenReturn("jwt-token");
        User user = new User();
        Role role = new Role();
        role.setRoleId(3L);
        user.setRole(role);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(rolePermissionRepository.findPermissionIdsByRoleId(3L)).thenReturn(List.of(1L));
        Permission permission = new Permission();
        permission.setPermissionId(1L);
        permission.setCode("DRIVERS_VIEW");
        when(permissionRepository.findAllById(List.of(1L))).thenReturn(List.of(permission));

        ResponseEntity<?> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        AuthResponse body = (AuthResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getUserId()).isEqualTo(10L);
        assertThat(body.getRoles()).contains("ROLE_ADMINISTRADOR");
        assertThat(body.getPermissions()).contains("DRIVERS_VIEW");
        assertThat(body.getPlantId()).isNull();
    }

    @Test
    void login_returnsConflictWhenCoordinatorHasNoPlant() {
        AuthRequest request = new AuthRequest();
        request.setUsername("coord");
        request.setPassword("secret");

        UserPrincipal principal = new UserPrincipal(
                20L,
                "coord",
                "secret",
                true,
                List.of(new SimpleGrantedAuthority("ROLE_COORDINADOR_PLANTA"))
        );

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtConfig.generateToken(principal)).thenReturn("jwt-token");
        when(userPlantRepository.findPlantCompanyInfo(eq(20L), eq(PageRequest.of(0, 1))))
                .thenReturn(List.of());

        ResponseEntity<?> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(
                "El usuario tiene ROLE_COORDINADOR_PLANTA pero no tiene planta asignada"
        );
    }

    @Test
    void login_returnsUnauthorizedForBadCredentials() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("bad");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        ResponseEntity<?> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Credenciales invalidas");
    }

    @Test
    void login_returnsUnauthorizedForInactiveUser() {
        AuthRequest request = new AuthRequest();
        request.setUsername("inactive");
        request.setPassword("secret");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("disabled"));

        ResponseEntity<?> response = controller.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("Usuario inactivo");
    }

    @Test
    void register_returnsBadRequestWhenUsernameTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");

        when(userRepository.findByUserName("existing")).thenReturn(new User());

        ResponseEntity<?> response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("El nombre de usuario ya esta en uso");
    }

    @Test
    void register_createsUserWithEncodedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("secret");
        request.setName("Juan");
        request.setLastName("Perez");
        request.setEmail("juan@example.com");
        request.setActive(true);
        request.setRoleId(3L);

        Role role = new Role();
        role.setRoleId(3L);

        when(userRepository.findByUserName("newuser")).thenReturn(null);
        when(passwordEncoder.encode("secret")).thenReturn("HASHED");
        when(roleRepository.findById(3L)).thenReturn(Optional.of(role));

        ResponseEntity<?> response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo("Usuario registrado correctamente");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUserName()).isEqualTo("newuser");
        assertThat(saved.getPassword()).isEqualTo("HASHED");
        assertThat(saved.getRole()).isEqualTo(role);
        assertThat(saved.getActive()).isTrue();
    }
}
