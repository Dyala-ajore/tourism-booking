package com.youruni.tourismbooking.notification;
import jakarta.validation.constraints.*;
public class NotificationDtoRequest {
    @NotBlank(message = "Recipient email cannot be blank")
    @Email(message = "Recipient email must be a valid email address")
    private String recipientEmail;
    @NotBlank(message = "Subject cannot be blank")
    @Size(min = 5, max = 200, message = "Subject must be between 5 and 200 characters")
    private String subject;
    @NotBlank(message = "Message cannot be blank")
    @Size(min = 10, max = 2000, message = "Message must be between 10 and 2000 characters")
    private String message;
    @NotNull(message = "Notification type cannot be null")
    private NotificationType notificationType;
    private Long relatedBookingId;
    private String notificationDetails;
    public NotificationDtoRequest() {
    }
    public NotificationDtoRequest(String recipientEmail, String subject, String message, NotificationType notificationType) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
    }
    public String getRecipientEmail() {
        return recipientEmail;
    }
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public NotificationType getNotificationType() {
        return notificationType;
    }
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }
    public Long getRelatedBookingId() {
        return relatedBookingId;
    }
    public void setRelatedBookingId(Long relatedBookingId) {
        this.relatedBookingId = relatedBookingId;
    }
    public String getNotificationDetails() {
        return notificationDetails;
    }
    public void setNotificationDetails(String notificationDetails) {
        this.notificationDetails = notificationDetails;
    }
}