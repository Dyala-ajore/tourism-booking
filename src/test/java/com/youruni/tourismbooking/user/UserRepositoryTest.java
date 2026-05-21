package com.youruni.tourismbooking.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser() {
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setPassword("password");
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);
        return user;
    }

    @Test
    void shouldSaveUser() {
        User saved = userRepository.saveAndFlush(createUser());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindByUsername() {
        userRepository.saveAndFlush(createUser());

        Optional<User> result = userRepository.findByUsername("john_doe");

        assertThat(result).isPresent();
    }

    @Test
    void shouldFindByEmail() {
        userRepository.saveAndFlush(createUser());

        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<User> result = userRepository.findByUsername("none");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldCheckUsernameExists() {
        userRepository.saveAndFlush(createUser());

        assertThat(userRepository.existsByUsername("john_doe")).isTrue();
    }

    @Test
    void shouldCheckEmailExists() {
        userRepository.saveAndFlush(createUser());

        assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
    }

    @Test
    void shouldFailDuplicateEmail() {
        userRepository.saveAndFlush(createUser());

        User duplicate = createUser();
        duplicate.setUsername("user2");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isNotNull();
    }

    @Test
    void shouldFailDuplicateUsername() {
        userRepository.saveAndFlush(createUser());

        User duplicate = createUser();
        duplicate.setEmail("user2@email.com");

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicate))
                .isNotNull();
    }
}