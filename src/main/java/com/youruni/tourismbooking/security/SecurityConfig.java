package com.youruni.tourismbooking.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.youruni.tourismbooking.common.ErrorResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.time.LocalDateTime;
import java.util.Arrays;
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    public SecurityConfig(UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }
    @Bean
    public RateLimitingFilter rateLimitingFilter(ObjectMapper objectMapper) {
        return new RateLimitingFilter(objectMapper);
    }
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper, RateLimitingFilter rateLimitingFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(401);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                401,
                                "Unauthorized: Authentication required",
                                request.getRequestURI()
                        );
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(403);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        ErrorResponse errorResponse = new ErrorResponse(
                                LocalDateTime.now(),
                                403,
                                "Forbidden: You do not have permission to access this resource",
                                request.getRequestURI()
                        );
                        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                    })
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/favicon.ico").permitAll()
                    .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/hotels").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.GET, "/api/hotels/**").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.POST, "/api/hotels").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/hotels/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/hotels/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/hotels/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/room-types").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.GET, "/api/room-types/**").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.POST, "/api/room-types").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/room-types/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/room-types/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/room-types/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/upcoming").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/bookings/my-history").hasRole("GUEST")
                .requestMatchers(HttpMethod.POST, "/api/bookings").hasRole("GUEST")
                .requestMatchers(HttpMethod.GET, "/api/bookings/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/bookings/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/availability").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/availability/check").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/availability").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.PUT, "/api/availability/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/availability/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/amenities").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/amenities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/amenities/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/amenities/hotels/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/amenities/hotels/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.POST, "/api/amenities/room-types/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.DELETE, "/api/amenities/room-types/**").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers(HttpMethod.GET, "/api/amenities").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.GET, "/api/amenities/**").hasAnyRole("ADMIN", "MANAGER", "GUEST")
                .requestMatchers(HttpMethod.POST, "/api/payments").hasRole("GUEST")
                .requestMatchers(HttpMethod.GET, "/api/payments/**").authenticated()
                .requestMatchers("/api/notifications/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitingFilter, AuthorizationFilter.class);
        return http.build();
    }
}