package com.youruni.tourismbooking.config;

import com.youruni.tourismbooking.util.JwtTestUtils;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public JwtTestUtils jwtTestUtils() {
        return new JwtTestUtils();
    }
}