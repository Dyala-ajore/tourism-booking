package com.youruni.tourismbooking.security;

import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "MySecretKeyForJWTTokenGenerationInTourismBookingSystemPleaseChangeInProduction12345";
    private static final long EXPIRATION = 86_400_000L; // 24h

    private User user;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "JWT_SECRET",    SECRET);
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", EXPIRATION);

        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("encoded_password");
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("john_doe")
                .password("encoded_password")
                .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                .build();
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  generateToken(User)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("generateToken(User)")
    class GenerateTokenFromUserTests {

        @Test
        @DisplayName(" Should generate non-blank token")
        void shouldGenerateNonBlankToken() {
            String token = jwtService.generateToken(user);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName(" Should embed username as subject")
        void shouldEmbedUsernameAsSubject() {
            String token = jwtService.generateToken(user);
            assertThat(jwtService.extractUsername(token)).isEqualTo("john_doe");
        }

        @Test
        @DisplayName(" Should produce a valid token immediately after generation")
        void shouldProduceValidToken() {
            String token = jwtService.generateToken(user);
            assertThat(jwtService.isValidToken(token)).isTrue();
        }

        @Test
        @DisplayName(" Should produce unique tokens on successive calls")
        void shouldProduceUniqueTokens() throws InterruptedException {
            String token1 = jwtService.generateToken(user);
            Thread.sleep(10);
            String token2 = jwtService.generateToken(user);
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  generateToken(UserDetails)
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("generateToken(UserDetails)")
    class GenerateTokenFromUserDetailsTests {

        @Test
        @DisplayName(" Should generate non-blank token from UserDetails")
        void shouldGenerateNonBlankToken() {
            String token = jwtService.generateToken(userDetails);
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName(" Should embed username as subject")
        void shouldEmbedUsername() {
            String token = jwtService.generateToken(userDetails);
            assertThat(jwtService.extractUsername(token)).isEqualTo("john_doe");
        }

        @Test
        @DisplayName(" Should produce a valid token")
        void shouldProduceValidToken() {
            String token = jwtService.generateToken(userDetails);
            assertThat(jwtService.isValidToken(token)).isTrue();
        }

        @Test
        @DisplayName(" Should handle UserDetails with no authorities")
        void shouldHandleNoAuthorities() {
            UserDetails noAuthUser = org.springframework.security.core.userdetails.User.builder()
                    .username("no_auth_user")
                    .password("pass")
                    .authorities(Collections.emptyList())
                    .build();

            String token = jwtService.generateToken(noAuthUser);
            assertThat(token).isNotBlank();
            assertThat(jwtService.extractUsername(token)).isEqualTo("no_auth_user");
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  extractUsername
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsernameTests {

        @Test
        @DisplayName(" Should extract correct username")
        void shouldExtractCorrectUsername() {
            String token = jwtService.generateToken(user);
            assertThat(jwtService.extractUsername(token)).isEqualTo("john_doe");
        }

        @Test
        @DisplayName(" Should return null for malformed token")
        void shouldReturnNullForMalformedToken() {
            assertThat(jwtService.extractUsername("not.a.valid.token")).isNull();
        }

        @Test
        @DisplayName(" Should return null for empty string")
        void shouldReturnNullForEmptyString() {
            assertThat(jwtService.extractUsername("")).isNull();
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  validateToken
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("validateToken()")
    class ValidateTokenTests {

        @Test
        @DisplayName(" Should validate token matching UserDetails")
        void shouldValidateMatchingToken() {
            String token = jwtService.generateToken(user);
            assertThat(jwtService.validateToken(token, userDetails)).isTrue();
        }

        @Test
        @DisplayName(" Should reject token for different user")
        void shouldRejectTokenForDifferentUser() {
            String token = jwtService.generateToken(user);

            UserDetails otherUser = org.springframework.security.core.userdetails.User.builder()
                    .username("other_user")
                    .password("pass")
                    .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                    .build();

            assertThat(jwtService.validateToken(token, otherUser)).isFalse();
        }

        @Test
        @DisplayName(" Should reject expired token")
        void shouldRejectExpiredToken() {
            ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", -1000L);
            String expiredToken = jwtService.generateToken(user);
            assertThat(jwtService.validateToken(expiredToken, userDetails)).isFalse();
        }

        @Test
        @DisplayName(" Should reject malformed token")
        void shouldRejectMalformedToken() {
            assertThat(jwtService.validateToken("bad.token.here", userDetails)).isFalse();
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    //  isValidToken
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•

    @Nested
    @DisplayName("isValidToken()")
    class IsValidTokenTests {

        @Test
        @DisplayName(" Should return true for fresh valid token")
        void shouldReturnTrueForValidToken() {
            String token = jwtService.generateToken(user);
            assertThat(jwtService.isValidToken(token)).isTrue();
        }

        @Test
        @DisplayName(" Should return false for expired token")
        void shouldReturnFalseForExpiredToken() {
            ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", -1000L);
            String expiredToken = jwtService.generateToken(user);
            assertThat(jwtService.isValidToken(expiredToken)).isFalse();
        }

        @Test
        @DisplayName(" Should return false for random string")
        void shouldReturnFalseForRandomString() {
            assertThat(jwtService.isValidToken("random.garbage.token")).isFalse();
        }

        @Test
        @DisplayName(" Should return false for empty string")
        void shouldReturnFalseForEmptyString() {
            assertThat(jwtService.isValidToken("")).isFalse();
        }

        @Test
        @DisplayName(" Should return false for token signed with different secret")
        void shouldReturnFalseForWrongSecret() {
            // Generate token with current service
            String token = jwtService.generateToken(user);

            // Create new service with different secret
            JwtService otherService = new JwtService();
            ReflectionTestUtils.setField(otherService, "JWT_SECRET",
                    "CompletelyDifferentSecretKeyThatIsLongEnough12345678901234567890");
            ReflectionTestUtils.setField(otherService, "JWT_EXPIRATION", EXPIRATION);

            assertThat(otherService.isValidToken(token)).isFalse();
        }
    }
}