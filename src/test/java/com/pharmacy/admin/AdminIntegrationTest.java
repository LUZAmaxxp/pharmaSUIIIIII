package com.pharmacy.admin;

import com.pharmacy.admin.entity.AdminUser;
import com.pharmacy.admin.repository.AdminUserRepository;
import com.pharmacy.shared.entity.Pharmacy;
import com.pharmacy.shared.entity.User;
import com.pharmacy.shared.repository.PharmacyRepository;
import com.pharmacy.shared.repository.UserRepository;
import com.pharmacy.shared.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminJwt;
    private Long pharmacyId;

    @BeforeEach
    void setUp() {
        adminUserRepository.deleteAll();
        pharmacyRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        User admin = new User();
        admin.setEmail("admin@pharmacy.ma");
        admin.setPassword(passwordEncoder.encode("Admin@1234"));
        admin.setRole("ROLE_ADMIN");
        User savedAdmin = userRepository.save(admin);

        AdminUser adminUser = new AdminUser(savedAdmin);
        adminUserRepository.save(adminUser);

        // Generate JWT for tests
        adminJwt = jwtUtil.generateToken(savedAdmin);

        // Seed a pharmacy
        Pharmacy pharmacy = new Pharmacy();
        pharmacy.setName("Pharmacie Test");
        pharmacy.setAddress("Casablanca");
        pharmacy.setActive(true);
        pharmacyId = pharmacyRepository.save(pharmacy).getId();
    }

    @Test
    void getUsers_withAdminJwt_shouldReturn200AndNonEmptyContent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").value("admin@pharmacy.ma"));
    }

    @Test
    void patchPharmacyStatus_shouldReturn200AndUpdatedActive() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/pharmacies/" + pharmacyId + "/status")
                        .header("Authorization", "Bearer " + adminJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void getDashboard_withAdminJwt_shouldReturn200WithTreatmentRatePercent() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.treatmentRatePercent").isNumber());
    }

    @Test
    void anyAdminEndpoint_withoutJwt_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }

    // ---- Auth endpoint tests (covers AuthController) ----

    @Test
    void loginWithValidCredentials_shouldReturn200WithToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@pharmacy.ma\",\"password\":\"Admin@1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void loginWithInvalidCredentials_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@pharmacy.ma\",\"password\":\"WrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---- Pharmacy GET tests (covers LoggingAdminPharmacyService + AdminPharmacyService) ----

    @Test
    void getPharmacies_withAdminJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pharmacies")
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getPharmacyById_withAdminJwt_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pharmacies/" + pharmacyId)
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pharmacyId));
    }

    // ---- User GET by ID test (covers AdminUserController.getUserById) ----

    @Test
    void getUserById_withAdminJwt_shouldReturn200() throws Exception {
        Long userId = userRepository.findByEmail("admin@pharmacy.ma").get().getId();
        mockMvc.perform(get("/api/v1/admin/users/" + userId)
                        .header("Authorization", "Bearer " + adminJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@pharmacy.ma"));
    }
}
