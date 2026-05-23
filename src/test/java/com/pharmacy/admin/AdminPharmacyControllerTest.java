package com.pharmacy.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.admin.controller.AdminPharmacyController;
import com.pharmacy.admin.dto.PharmacySummaryDTO;
import com.pharmacy.admin.dto.PharmacyStatusUpdateDTO;
import com.pharmacy.admin.service.AdminPharmacyServiceI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for AdminPharmacyController.
 *
 * @WebMvcTest scans Filter beans, so JwtAuthenticationFilter is picked up.
 * Its dependencies (JwtUtil, UserRepository) are not in the web slice —
 * we satisfy them with @MockBean.  The real filter passes through on requests
 * that carry no Authorization header, which is the case for all tests below
 * (authentication is provided by @WithMockUser instead).
 *
 * A nested @TestConfiguration replaces Spring Boot's default SecurityFilterChain
 * with one that has CSRF disabled and the same hasRole("ADMIN") rule as production.
 */
@WebMvcTest(AdminPharmacyController.class)
@Import(com.pharmacy.admin.exception.GlobalExceptionHandler.class)
class AdminPharmacyControllerTest {

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminPharmacyServiceI adminPharmacyService;

    // Satisfy JwtAuthenticationFilter constructor (scanned as a Filter by @WebMvcTest)
    @MockBean private com.pharmacy.shared.security.JwtUtil jwtUtil;
    @MockBean private com.pharmacy.shared.repository.UserRepository userRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllPharmacies_shouldReturn200() throws Exception {
        PharmacySummaryDTO dto = new PharmacySummaryDTO(1L, "Pharmacie Test", "Casablanca", true, 33.5, -7.6);
        when(adminPharmacyService.getAllPharmacies(any()))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(java.util.List.of(dto)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/admin/pharmacies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPharmacyById_shouldReturn200() throws Exception {
        PharmacySummaryDTO dto = new PharmacySummaryDTO(1L, "Pharmacie Test", "Casablanca", true, 33.5, -7.6);
        when(adminPharmacyService.getPharmacyById(eq(1L))).thenReturn(dto);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/admin/pharmacies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_shouldReturn500_whenServiceThrowsUnexpectedException() throws Exception {
        when(adminPharmacyService.updateStatus(eq(1L), eq(true), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(patch("/api/v1/admin/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_shouldReturn200_withValidBody() throws Exception {
        PharmacySummaryDTO dto = new PharmacySummaryDTO(1L, "Pharmacie Test", "Casablanca", false, 33.5, -7.6);
        when(adminPharmacyService.updateStatus(eq(1L), eq(false), any())).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/admin/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PharmacyStatusUpdateDTO(false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.name").value("Pharmacie Test"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_shouldReturn400_whenActiveFieldMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void updateStatus_shouldReturn403_withoutAdminRole() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/pharmacies/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isForbidden());
    }
}
