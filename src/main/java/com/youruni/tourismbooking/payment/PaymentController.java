package com.youruni.tourismbooking.payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
@Tag(name = "Payments", description = "Payment management API")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @Operation(summary = "Create new payment")
    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    public ResponseEntity<PaymentDtoResponse> createPayment(
            @Valid @RequestBody PaymentDtoRequest request,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        PaymentDtoResponse response = paymentService.processPayment(request, authentication.getName(), userRole);
        java.net.URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @Operation(summary = "Get payment by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PaymentDtoResponse> getPayment(
            @PathVariable Long id,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        return ResponseEntity.ok(paymentService.getPaymentById(id, authentication.getName(), userRole));
    }
    @Operation(summary = "Get payments by booking ID")
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<java.util.List<PaymentDtoResponse>> getPaymentsByBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId, authentication.getName(), userRole));
    }
    @Operation(summary = "Refund payment")
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PaymentDtoResponse> refund(
            @PathVariable Long id,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        return ResponseEntity.ok(paymentService.refundPayment(id, authentication.getName(), userRole));
    }
}