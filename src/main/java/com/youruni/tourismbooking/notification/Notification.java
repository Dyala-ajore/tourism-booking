package com.youruni.tourismbooking.notification;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String recipientEmail;
    @Column(nullable = false, length = 200)
    private String subject;
    @Column(nullable = false, length = 2000)
    private String message;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;
    @Column(name = "related_booking_id")
    private Long relatedBookingId;
    @Column(length = 500)
    private String notificationDetails;
    private LocalDateTime sentAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    public Notification() {
    }
    public Notification(String recipientEmail, String subject, String message, NotificationType notificationType) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.status = NotificationStatus.PENDING;
    }
    public Notification(String recipientEmail, String subject, String message, NotificationType notificationType, Long relatedBookingId) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
        this.notificationType = notificationType;
        this.relatedBookingId = relatedBookingId;
        this.status = NotificationStatus.PENDING;
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