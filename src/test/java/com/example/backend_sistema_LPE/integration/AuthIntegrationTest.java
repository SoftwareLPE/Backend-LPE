package com.example.backend_sistema_LPE.integration;

import com.example.backend_sistema_LPE.dto.AuthRequest;
import com.example.backend_sistema_LPE.model.Role;
import com.example.backend_sistema_LPE.model.User;
import com.example.backend_sistema_LPE.repository.RoleRepository;
import com.example.backend_sistema_LPE.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role role = new Role();
        role.setRoleName("ADMINISTRADOR");
        role = roleRepository.save(role);

        User user = new User();
        user.setUserName("admin");
        user.setPassword(passwordEncoder.encode("secret"));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }

    @Test
    void login_returnsToken() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("secret");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMINISTRADOR"));
    }

    @Test
    void login_rejectsInvalidPassword() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("bad");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
