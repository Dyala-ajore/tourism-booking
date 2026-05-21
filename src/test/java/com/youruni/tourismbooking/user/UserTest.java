package com.youruni.tourismbooking.user;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        User user = new User();

        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setFullName("John Doe");
        user.setPassword("password");
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);

        assertEquals("john_doe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertTrue(user.getEnabled());
    }

    @Test
    void shouldSetTimestampsOnCreate() {
        User user = new User();

        user.onCreate();

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void shouldUpdateTimestampOnUpdate() {
        User user = new User();

        user.onCreate();
        LocalDateTime before = user.getUpdatedAt();

        user.onUpdate();

        assertTrue(user.getUpdatedAt().isAfter(before)
                || user.getUpdatedAt().isEqual(before));
    }
}