package com.youruni.tourismbooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();

        userDetails = User.builder()
                .username("john_doe")
                .password("pass")
                .authorities(new SimpleGrantedAuthority("ROLE_GUEST"))
                .build();
    }

    @Test
    void validToken_shouldAuthenticateUser() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtService.isValidToken("token")).thenReturn(true);
        when(jwtService.extractUsername("token")).thenReturn("john_doe");
        when(userDetailsService.loadUserByUsername("john_doe")).thenReturn(userDetails);
        when(jwtService.validateToken("token", userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void noHeader_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void invalidToken_shouldNotAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(jwtService.isValidToken("bad")).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}