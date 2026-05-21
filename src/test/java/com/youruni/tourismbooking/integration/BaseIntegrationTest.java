package com.youruni.tourismbooking.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youruni.tourismbooking.security.JwtService;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected UserRepository userRepository;
    @Autowired protected JwtService jwtService;
    @Autowired protected PasswordEncoder passwordEncoder;

    protected String bearerToken;

    protected String testUsername;
    protected String testPassword = "TestPassword@123";
    protected String testEmail;

    /**
     * Creates a real test user directly in the test database and generates a JWT.
     *
     * Why this is better for integration tests:
     * - It avoids calling /auth/register repeatedly.
     * - It prevents RateLimitingFilter from returning 429 Too Many Requests.
     * - It guarantees that getBearerTokenHeader() never returns "Bearer null".
     */
    protected void obtainUserToken() {
        String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        testUsername = "user_" + unique;
        testEmail = "user_" + unique + "@test.com";

        User user = new User();
        user.setUsername(testUsername);
        user.setEmail(testEmail);
        user.setFullName("Test User");
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);

        // Some versions of your User entity require createdAt/updatedAt as NOT NULL.
        // Reflection keeps this base test compatible even if the setters do not exist.
        setIfExists(user, "setCreatedAt", LocalDateTime.now());
        setIfExists(user, "setUpdatedAt", LocalDateTime.now());

        User savedUser = userRepository.saveAndFlush(user);
        bearerToken = jwtService.generateToken(savedUser);
    }

    protected String getBearerTokenHeader() {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalStateException("JWT token was not generated. Call obtainUserToken() in @BeforeEach first.");
        }
        return "Bearer " + bearerToken;
    }

    private void setIfExists(User user, String methodName, LocalDateTime value) {
        try {
            Method method = User.class.getMethod(methodName, LocalDateTime.class);
            method.invoke(user, value);
        } catch (Exception ignored) {
            // Entity does not expose this setter, or it is handled by @PrePersist/@PreUpdate.
        }
    }
}
