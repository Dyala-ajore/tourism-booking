package com.youruni.tourismbooking.notification;
import java.time.LocalDateTime;
public class NotificationDtoResponse {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String message;
    private NotificationType notificationType;
    private NotificationStatus status;
    private Long relatedBookingId;
    private String notificationDetails;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    public NotificationDtoResponse() {
    }
    public NotificationDtoResponse(Long id, String recipientEmail, String subject,
                                   NotificationType notificationType, NotificationStatus status) {
        this.id = id;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.notificationType = notificationType;
        this.status = status;
    }
    public NotificationDtoResponse(Long id, String recipientEmail, String subject, String message,
                                   NotificationType notificationType, NotificationStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.status = status;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public NotificationStatus getStatus() {
        return status;
    }
    public void setStatus(NotificationStatus status) {
        this.status = status;
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
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}