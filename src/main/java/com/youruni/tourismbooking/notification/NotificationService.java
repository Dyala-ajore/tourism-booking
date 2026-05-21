package com.youruni.tourismbooking.notification;
import java.util.List;
public interface NotificationService {
    NotificationDtoResponse sendNotification(NotificationDtoRequest request);
    void sendBookingNotification(Long bookingId, NotificationType type);
    NotificationDtoResponse getNotificationById(Long id);
    List<NotificationDtoResponse> getNotificationsByBookingId(Long bookingId);
}