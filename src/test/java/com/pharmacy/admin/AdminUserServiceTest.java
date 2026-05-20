package com.pharmacy.admin;

import com.pharmacy.admin.dto.UserSummaryDTO;
import com.pharmacy.admin.service.AdminUserService;
import com.pharmacy.shared.entity.User;
import com.pharmacy.shared.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("patient@mail.com");
        sampleUser.setRole("ROLE_PATIENT");
        sampleUser.setCreatedAt(LocalDateTime.of(2026, 1, 10, 9, 0));
    }

    @Test
    void getAllUsers_shouldReturnMappedDTOs() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(sampleUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        Page<UserSummaryDTO> result = adminUserService.getAllUsers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("patient@mail.com");
        assertThat(result.getContent().get(0).role()).isEqualTo("ROLE_PATIENT");
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void getUserById_shouldReturnDTO_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserSummaryDTO dto = adminUserService.getUserById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.email()).isEqualTo("patient@mail.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_shouldThrowEntityNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminUserService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User with id 99 not found");
        verify(userRepository, times(1)).findById(99L);
    }
}
