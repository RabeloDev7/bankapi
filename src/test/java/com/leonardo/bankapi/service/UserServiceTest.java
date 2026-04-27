package com.leonardo.bankapi.service;

import com.leonardo.bankapi.dto.UserRequest;
import com.leonardo.bankapi.dto.UserResponse;
import com.leonardo.bankapi.entity.User;
import com.leonardo.bankapi.exception.ResourceNotFoundException;
import com.leonardo.bankapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — testes unitários")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User user;
    private UserRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).name("Leonardo").email("leo@email.com").password("hashed")
                .build();

        request = new UserRequest();
        request.setName("Leonardo");
        request.setEmail("leo@email.com");
        request.setPassword("123456");
    }

    // ── createUser ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createUser: sucesso")
    void createUser_success() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("leo@email.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createUser: email duplicado lança IllegalArgumentException")
    void createUser_emailDuplicado() {
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email já cadastrado");

        verify(userRepository, never()).save(any());
    }

    // ── getById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getById: retorna usuário existente")
    void getById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertThat(userService.getById(1L).getEmail()).isEqualTo("leo@email.com");
    }

    @Test
    @DisplayName("getById: ID inexistente lança ResourceNotFoundException")
    void getById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    // ── getAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAll: retorna lista completa")
    void getAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        assertThat(userService.getAll()).hasSize(1);
    }

    // ── update ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: sucesso sem conflito de email")
    void update_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed2");
        when(userRepository.save(any())).thenReturn(user);

        UserResponse response = userService.update(1L, request);
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("update: email pertence a outro usuário lança IllegalArgumentException")
    void update_emailConflict() {
        User outro = User.builder().id(2L).email("leo@email.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(outro));

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete: sucesso")
    void delete_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        assertThatCode(() -> userService.delete(1L)).doesNotThrowAnyException();
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("delete: ID inexistente lança ResourceNotFoundException")
    void delete_notFound() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
