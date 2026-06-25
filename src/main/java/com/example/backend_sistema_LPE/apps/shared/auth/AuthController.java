package com.example.backend_sistema_LPE.apps.shared.auth;

import com.example.backend_sistema_LPE.apps.shared.plant.PlantCompanyInfoDTO;
import com.example.backend_sistema_LPE.apps.shared.user.User;
import com.example.backend_sistema_LPE.apps.shared.permission.PermissionRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RolePermissionRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RoleRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserPlantRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserRepository;
import com.example.backend_sistema_LPE.apps.shared.security.JwtConfig;
import com.example.backend_sistema_LPE.apps.shared.security.MyUserDetailsService;
import com.example.backend_sistema_LPE.apps.shared.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final MyUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserPlantRepository userPlantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionRepository permissionRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtConfig jwtConfig,
            MyUserDetailsService userDetailsService,
            UserRepository userRepository,
            UserPlantRepository userPlantRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            RolePermissionRepository rolePermissionRepository,
            PermissionRepository permissionRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.userPlantRepository = userPlantRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.permissionRepository = permissionRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println(">>> Entro al metodo /auth/login");
        System.out.println(">>> username=" + request.getUsername());
        System.out.println(">>> password=" + (request.getPassword() == null ? "null" : "[set]"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            String token = jwtConfig.generateToken(principal);

            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            List<String> permissions = List.of();
            if (principal.getUserId() != null) {
                User user = userRepository.findById(principal.getUserId()).orElse(null);
                if (user != null && user.getRole() != null) {
                    List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(
                            user.getRole().getRoleId()
                    );
                    permissions = permissionRepository.findAllById(permissionIds).stream()
                            .map(permission -> permission.getCode())
                            .distinct()
                            .toList();
                }
            }

            Long plantId = null;
            String plantName = null;
            Long companyId = null;
            String companyName = null;

            if (roles.contains("ROLE_COORDINADOR_PLANTA")) {
                var infoList = userPlantRepository.findPlantCompanyInfo(
                        principal.getUserId(),
                        org.springframework.data.domain.PageRequest.of(0, 1)
                );

                if (infoList.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("El usuario tiene ROLE_COORDINADOR_PLANTA pero no tiene planta asignada");
                }

                PlantCompanyInfoDTO info = infoList.get(0);
                plantId = info.getPlantId();
                plantName = info.getPlantName();
                companyId = info.getCompanyId();
                companyName = info.getCompanyName();
            }

            return ResponseEntity.ok(new AuthResponse(
                    token,
                    principal.getUserId(),
                    principal.getUsername(),
                    roles,
                    permissions,
                    plantId,
                    plantName,
                    companyId,
                    companyName
            ));

        } catch (BadCredentialsException ex) {
            System.out.println(">>> Credenciales invalidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales invalidas");
        } catch (DisabledException ex) {
            System.out.println(">>> Usuario inactivo");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario inactivo");
        } catch (Exception ex) {
            System.out.println(">>> Error en /auth/login:");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno en el login");
        }
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println(">>> Entro al metodo /auth/register");

        try {
            String normalizedEmail = normalizeOptionalEmail(request.getEmail());

            if (userRepository.findByUserName(request.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre de usuario ya esta en uso");
            }

            if (normalizedEmail != null && userRepository.existsByEmail(normalizedEmail)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El email ya esta en uso");
            }

            User user = new User();
            user.setUserName(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setLastName(request.getLastName());
            user.setEmail(normalizedEmail);
            user.setActive(request.getActive());
            user.setRole(roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId())));

            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Usuario registrado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el usuario");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return ResponseEntity.ok(Map.of(
                "userId", principal.getUserId(),
                "username", principal.getUsername(),
                "roles", roles
        ));
    }

    private String normalizeOptionalEmail(String email) {
        if (email == null) {
            return null;
        }

        String normalized = email.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
