package com.youruni.tourismbooking.notification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification management API")
public class NotificationController {
    private final NotificationService notificationService;
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send notification manually (Admin only)")
    public ResponseEntity<NotificationDtoResponse> sendNotification(
            @Valid @RequestBody NotificationDtoRequest request) {
        return ResponseEntity.ok(notificationService.sendNotification(request));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notification by ID (Admin only)")
    public ResponseEntity<NotificationDtoResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get notifications by booking ID (Admin only)")
    public ResponseEntity<List<NotificationDtoResponse>> getByBookingId(
            @RequestParam Long bookingId) {
        return ResponseEntity.ok(
                notificationService.getNotificationsByBookingId(bookingId)
        );
    }
}