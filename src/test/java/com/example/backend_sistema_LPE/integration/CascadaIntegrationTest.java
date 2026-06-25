package com.example.backend_sistema_LPE.integration;

import com.example.backend_sistema_LPE.apps.shared.auth.AuthRequest;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.Company;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.Driver;
import com.example.backend_sistema_LPE.apps.shared.plant.Plant;
import com.example.backend_sistema_LPE.apps.shared.role.Role;
import com.example.backend_sistema_LPE.apps.shared.user.User;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.company.CompanyRepository;
import com.example.backend_sistema_LPE.apps.trip_cascade_backend.driver.DriverRepository;
import com.example.backend_sistema_LPE.apps.shared.plant.PlantRepository;
import com.example.backend_sistema_LPE.apps.shared.role.RoleRepository;
import com.example.backend_sistema_LPE.apps.shared.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CascadaIntegrationTest {

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
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private PlantRepository plantRepository;
    @Autowired
    private DriverRepository driverRepository;

    private String token;
    private Long plantId;

    @BeforeEach
    void setUp() throws Exception {
        driverRepository.deleteAll();
        plantRepository.deleteAll();
        companyRepository.deleteAll();
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

        Company company = new Company();
        company.setCompanyName("ACME");
        company = companyRepository.save(company);

        Plant plant = new Plant();
        plant.setPlantName("Planta 1");
        plant.setCompany(company);
        plant = plantRepository.save(plant);
        plantId = plant.getPlantId();

        Driver driver = new Driver();
        driver.setDriverName("Juan");
        driver.setLastName("Perez");
        driver.setActive(true);
        driver.setPlant(plant);
        driverRepository.save(driver);

        token = loginAndGetToken();
    }

    @Test
    void putAndGetCascada_returnsSavedRows() throws Exception {
        LocalDate weekDate = LocalDate.of(2026, 2, 6);
        Long driverId = driverRepository.findAll().get(0).getDriverId();

        Map<String, Object> payload = Map.of(
                "plantId", plantId,
                "weekDate", weekDate.toString(),
                "shiftId", "1",
                "days", Map.of(
                        "lun", List.of(
                                Map.of(
                                        "driverId", driverId,
                                        "E", "R01",
                                        "S", "R01",
                                        "ETE", "",
                                        "STE", ""
                                )
                        )
                )
        );

        mockMvc.perform(put("/cascada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNoContent());

        MvcResult result = mockMvc.perform(get("/cascada")
                        .param("plantId", plantId.toString())
                        .param("weekDate", weekDate.toString())
                        .param("shiftId", "1")
                        .param("dayKey", "lun")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(response.get("rows")).isNotNull();
        assertThat(response.get("rows").size()).isEqualTo(1);
        assertThat(response.get("rows").get(0).get("E").asText()).isEqualTo("R01");
    }

    private String loginAndGetToken() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("secret");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }
}
