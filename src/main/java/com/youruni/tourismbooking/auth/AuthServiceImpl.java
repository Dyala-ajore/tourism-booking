package com.youruni.tourismbooking.auth;

import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.security.JwtService;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed: Username '{}' already exists", request.getUsername());
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email '{}' already exists", request.getEmail());
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        logger.info("User registered successfully: {}", savedUser.getUsername());

        String token = jwtService.generateToken(savedUser);

        AuthResponse response = new AuthResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setFullName(savedUser.getFullName());
        response.setRole(savedUser.getRole());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setMessage("Registration successful");

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for: {}", request.getUsernameOrEmail());

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> {
                    logger.warn("Login failed: User '{}' not found", request.getUsernameOrEmail());
                    return new BadRequestException("Invalid username/email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            logger.warn("Login failed: Invalid password for user '{}'", user.getUsername());
            throw new BadRequestException("Invalid username/email or password");
        }

        if (!user.getEnabled()) {
            logger.warn("Login failed: User '{}' is disabled", user.getUsername());
            throw new BadRequestException("User account is disabled");
        }

        logger.info("User logged in successfully: {}", user.getUsername());

        String token = jwtService.generateToken(user);

        AuthResponse response = new AuthResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole());
        response.setToken(token);
        response.setTokenType("Bearer");
        response.setMessage("Login successful");

        return response;
    }
}