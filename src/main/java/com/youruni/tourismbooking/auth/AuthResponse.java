package com.youruni.tourismbooking.auth;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.youruni.tourismbooking.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Authentication response containing user info and JWT token")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponse {
    @Schema(description = "User ID", example = "1")
    private Long id;
    @Schema(description = "Username", example = "john_doe")
    private String username;
    @Schema(description = "Email address", example = "john@example.com")
    private String email;
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;
    @Schema(description = "User role", example = "GUEST")
    private UserRole role;
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    @Schema(description = "Token type", example = "Bearer")
    private String tokenType = "Bearer";
    @Schema(description = "Success message", example = "Registration successful")
    private String message;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getTokenType() {
        return tokenType;
    }
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}