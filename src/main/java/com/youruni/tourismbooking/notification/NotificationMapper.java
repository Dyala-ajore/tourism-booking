package com.youruni.tourismbooking.notification;
import org.springframework.stereotype.Component;
@Component
public class NotificationMapper {
    public NotificationDtoResponse toResponseDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDtoResponse response = new NotificationDtoResponse();
        response.setId(notification.getId());
        response.setRecipientEmail(notification.getRecipientEmail());
        response.setSubject(notification.getSubject());
        response.setMessage(notification.getMessage());
        response.setNotificationType(notification.getNotificationType());
        response.setStatus(notification.getStatus());
        response.setRelatedBookingId(notification.getRelatedBookingId());
        response.setNotificationDetails(notification.getNotificationDetails());
        response.setSentAt(notification.getSentAt());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }
    public Notification toEntity(NotificationDtoRequest requestDto) {
        if (requestDto == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setRecipientEmail(requestDto.getRecipientEmail());
        notification.setSubject(requestDto.getSubject());
        notification.setMessage(requestDto.getMessage());
        notification.setNotificationType(requestDto.getNotificationType());
        notification.setRelatedBookingId(requestDto.getRelatedBookingId());
        notification.setNotificationDetails(requestDto.getNotificationDetails());
        notification.setStatus(NotificationStatus.PENDING);
        return notification;
    }
    public void updateEntityFromDto(NotificationDtoRequest requestDto, Notification notification) {
        if (requestDto == null || notification == null) {
            return;
        }
        if (requestDto.getRecipientEmail() != null) {
            notification.setRecipientEmail(requestDto.getRecipientEmail());
        }
        if (requestDto.getSubject() != null) {
            notification.setSubject(requestDto.getSubject());
        }
        if (requestDto.getMessage() != null) {
            notification.setMessage(requestDto.getMessage());
        }
        if (requestDto.getNotificationType() != null) {
            notification.setNotificationType(requestDto.getNotificationType());
        }
        if (requestDto.getRelatedBookingId() != null) {
            notification.setRelatedBookingId(requestDto.getRelatedBookingId());
        }
        if (requestDto.getNotificationDetails() != null) {
            notification.setNotificationDetails(requestDto.getNotificationDetails());
        }
    }
}