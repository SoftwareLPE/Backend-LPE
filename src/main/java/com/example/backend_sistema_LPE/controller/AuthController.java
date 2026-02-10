package com.example.backend_sistema_LPE.controller;

import com.example.backend_sistema_LPE.dto.PlantCompanyInfoDTO;
import com.example.backend_sistema_LPE.dto.RegisterRequest;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.RoleRepository;
import com.example.backend_sistema_LPE.repository.UserPlantRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.example.backend_sistema_LPE.security.JwtConfig;
import com.example.backend_sistema_LPE.security.MyUserDetailsService;
import com.example.backend_sistema_LPE.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.example.backend_sistema_LPE.dto.AuthRequest;
import com.example.backend_sistema_LPE.dto.AuthResponse;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:8081")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final MyUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserPlantRepository userPlantRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtConfig jwtConfig, MyUserDetailsService userDetailsService, UserRepository userRepository, UserPlantRepository userPlantRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.userPlantRepository = userPlantRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println(">>> Entró al método /auth/login");



        try {

            // 1. Autenticar usuario con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Guardar autenticación en el contexto (opcional pero correcto)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. Obtener el usuario autenticado
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            // o si prefieres, nuevamente desde tu servicio:
            // UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            // 4. Generar token
            String token = jwtConfig.generateToken(principal);

            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            // Valores por defecto (para roles distintos)
            Long plantId = null;
            String plantName = null;
            Long companyId = null;
            String companyName = null;

            // Solo si es coordinador de planta
            if (roles.contains("ROLE_COORDINADOR_PLANTA")) {

                var infoList = userPlantRepository.findPlantCompanyInfo(
                        principal.getUserId(),
                        org.springframework.data.domain.PageRequest.of(0, 1)
                );

                if (infoList.isEmpty()) {
                    // Política recomendada: si este rol requiere planta, devuelve 409/400
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("El usuario tiene ROLE_COORDINADOR_PLANTA pero no tiene planta asignada");
                }

                PlantCompanyInfoDTO info = infoList.get(0);
                plantId = info.getPlantId();
                plantName = info.getPlantName();
                companyId = info.getCompanyId();
                companyName = info.getCompanyName();
            }


                // 5. Responder OK con el token
            return ResponseEntity.ok(new AuthResponse(
                    token,
                    principal.getUserId(),
                    principal.getUsername(),
                    roles,
                    plantId,
                    plantName,
                    companyId,
                    companyName

            ));

        } catch (BadCredentialsException ex) {
            // Credenciales incorrectas → 401
            System.out.println(">>> Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciales inválidas");
        } catch (Exception ex) {
            // Cualquier otro error lo vemos en consola y devolvemos 500
            System.out.println(">>> Error en /auth/login:");
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno en el login");
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println(">>> Entró al método /auth/register");

        try {
            // 1. Validar si el usuario ya existe
            // (asumiendo que usas userRepository y entidad User)
            if (userRepository.findByUserName(request.getUsername()) != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nombre de usuario ya está en uso");
            }

            // 2. Crear entidad User
            User user = new User();
            user.setUserName(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword())); // IMPORTANTE: encriptar
            user.setName(request.getName());
            user.setLastName(request.getLastName());
            user.setEmail(request.getEmail());
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


}
