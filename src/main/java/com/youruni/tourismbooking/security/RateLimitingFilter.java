package com.youruni.tourismbooking.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youruni.tourismbooking.common.ErrorResponse;
import com.youruni.tourismbooking.common.RateLimitStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalDateTime;
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final int REQUEST_LIMIT = 5;
    private static final long WINDOW_DURATION_MS = 60 * 1000; 
    private static final int RESPONSE_STATUS_429 = 429;
    private final RateLimitStore rateLimitStore;
    private final ObjectMapper objectMapper;
    private static final String[] EXCLUDED_PATTERNS = {
            "/swagger-ui",
            "/v3/api-docs",
            "/uploads"
    };
    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.rateLimitStore = new RateLimitStore(REQUEST_LIMIT, WINDOW_DURATION_MS);
        this.objectMapper = objectMapper;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (isExcludedPath(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        String rateLimitKey = buildRateLimitKey(request);
        if (!rateLimitStore.allowRequest(rateLimitKey)) {
            sendRateLimitResponse(response, request);
            return;
        }
        filterChain.doFilter(request, response);
    }
    private String buildRateLimitKey(HttpServletRequest request) {
        String identifier = getIdentifier(request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        return identifier + ":" + method + ":" + path;
    }
    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal().toString()))) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                String username = null;
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                } else if (principal instanceof String) {
                    username = (String) principal;
                }
                if (username != null && !username.isEmpty()) {
                    return "user:" + username;
                }
            }
        }
        String clientIp = getClientIp(request);
        return "ip:" + clientIp;
    }
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    private boolean isExcludedPath(String requestPath) {
        for (String pattern : EXCLUDED_PATTERNS) {
            if (requestPath.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
    private void sendRateLimitResponse(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setStatus(RESPONSE_STATUS_429);
        response.setContentType("application/json");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                RESPONSE_STATUS_429,
                "Too many requests. Please try again later.",
                request.getRequestURI(),
                null
        );
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }
}