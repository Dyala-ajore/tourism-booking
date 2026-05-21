package com.youruni.tourismbooking.security;

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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl Tests")
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User activeUser;
    private User disabledUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("john_doe");
        activeUser.setEmail("john@example.com");
        activeUser.setPassword("encoded_password");
        activeUser.setRole(UserRole.GUEST);
        activeUser.setEnabled(true);

        disabledUser = new User();
        disabledUser.setId(2L);
        disabledUser.setUsername("disabled_user");
        disabledUser.setEmail("disabled@example.com");
        disabledUser.setPassword("encoded_password");
        disabledUser.setRole(UserRole.GUEST);
        disabledUser.setEnabled(false);

        adminUser = new User();
        adminUser.setId(3L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("admin_encoded");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Load by username
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("loadUserByUsername() â€” found by username")
    class LoadByUsernameTests {

        @Test
        @DisplayName(" Should return UserDetails when user found by username")
        void shouldReturnUserDetailsForValidUsername() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(activeUser));

            UserDetails result = userDetailsService.loadUserByUsername("john_doe");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");
            assertThat(result.getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName(" Should assign ROLE_ prefixed authority")
        void shouldAssignRolePrefixedAuthority() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(activeUser));

            UserDetails result = userDetailsService.loadUserByUsername("john_doe");

            assertThat(result.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        }

        @Test
        @DisplayName(" Should assign ROLE_ADMIN for admin user")
        void shouldAssignAdminRole() {
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

            UserDetails result = userDetailsService.loadUserByUsername("admin");

            assertThat(result.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        @Test
        @DisplayName(" Should mark account as enabled for active user")
        void shouldMarkActiveUserAsEnabled() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(activeUser));

            UserDetails result = userDetailsService.loadUserByUsername("john_doe");

            assertThat(result.isEnabled()).isTrue();
            assertThat(result.isAccountNonLocked()).isTrue();
        }

        @Test
        @DisplayName(" Should not call findByEmail when username match found")
        void shouldNotCallFindByEmailWhenUsernameFound() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(activeUser));

            userDetailsService.loadUserByUsername("john_doe");

            verify(userRepository, never()).findByEmail(any());
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Load by email (fallback)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("loadUserByUsername() â€” fallback to email")
    class LoadByEmailFallbackTests {

        @Test
        @DisplayName(" Should return UserDetails when user found by email")
        void shouldReturnUserDetailsForValidEmail() {
            when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));

            UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("john_doe");
        }

        @Test
        @DisplayName(" Should assign correct role when found by email")
        void shouldAssignCorrectRoleWhenFoundByEmail() {
            when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));

            UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

            assertThat(result.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_GUEST"));
        }

        @Test
        @DisplayName(" Should throw UsernameNotFoundException when not found by username or email")
        void shouldThrowWhenNotFoundByUsernameOrEmail() {
            when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("unknown");
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Disabled user
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("loadUserByUsername() â€” disabled user")
    class DisabledUserTests {

        @Test
        @DisplayName(" Should return UserDetails for disabled user (disabled flag set)")
        void shouldReturnUserDetailsForDisabledUser() {
            when(userRepository.findByUsername("disabled_user")).thenReturn(Optional.of(disabledUser));

            UserDetails result = userDetailsService.loadUserByUsername("disabled_user");

            assertThat(result).isNotNull();
            assertThat(result.isEnabled()).isFalse();
        }

        @Test
        @DisplayName(" Should mark account as locked for disabled user")
        void shouldMarkAccountAsLockedForDisabledUser() {
            when(userRepository.findByUsername("disabled_user")).thenReturn(Optional.of(disabledUser));

            UserDetails result = userDetailsService.loadUserByUsername("disabled_user");

            assertThat(result.isAccountNonLocked()).isFalse();
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  Repository interaction
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("Repository interaction")
    class RepositoryInteractionTests {

        @Test
        @DisplayName(" Should call findByUsername exactly once")
        void shouldCallFindByUsernameOnce() {
            when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(activeUser));

            userDetailsService.loadUserByUsername("john_doe");

            verify(userRepository, times(1)).findByUsername("john_doe");
        }

        @Test
        @DisplayName(" Should call findByEmail exactly once when username not found")
        void shouldCallFindByEmailOnceWhenUsernameNotFound() {
            when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(activeUser));

            userDetailsService.loadUserByUsername("john@example.com");

            verify(userRepository, times(1)).findByEmail("john@example.com");
        }

        @Test
        @DisplayName(" Should throw with identifier in message when user not found")
        void shouldIncludeIdentifierInExceptionMessage() {
            when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
            when(userRepository.findByEmail("ghost")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("ghost");
        }
    }
}