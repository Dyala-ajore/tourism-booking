package com.youruni.tourismbooking.auth;

import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.security.JwtService;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setUsername("john_doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("john_doe");
        loginRequest.setPassword("password123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john_doe");
        savedUser.setEmail("john@example.com");
        savedUser.setFullName("John Doe");
        savedUser.setPassword("encoded_password");
        savedUser.setRole(UserRole.GUEST);
        savedUser.setEnabled(true);
    }

    // ========================= REGISTER =========================

    @Nested
    @DisplayName("register()")
    class RegisterTests {

        @Test
        void shouldRegisterUserSuccessfully() {

            when(userRepository.existsByUsername("john_doe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(savedUser)).thenReturn("mock.jwt.token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getUsername()).isEqualTo("john_doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getFullName()).isEqualTo("John Doe");
            assertThat(response.getRole()).isEqualTo(UserRole.GUEST);
            assertThat(response.getToken()).isEqualTo("mock.jwt.token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        void shouldThrowWhenUsernameExists() {

            when(userRepository.existsByUsername("john_doe")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Username already exists");
        }
    }

    // ========================= LOGIN =========================

    @Nested
    @DisplayName("login()")
    class LoginTests {

        @Test
        void shouldLoginSuccessfully() {

            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwtService.generateToken(savedUser)).thenReturn("token");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("token");
        }

        @Test
        void shouldThrowWhenUserNotFound() {

            when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void shouldThrowWhenPasswordWrong() {

            when(userRepository.findByUsername(any())).thenReturn(Optional.of(savedUser));
            when(passwordEncoder.matches(any(), any())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}