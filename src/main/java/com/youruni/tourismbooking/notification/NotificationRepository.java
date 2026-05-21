package com.youruni.tourismbooking.notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByStatus(NotificationStatus status);
    List<Notification> findByRecipientEmail(String recipientEmail);
    List<Notification> findByNotificationTypeAndStatus(NotificationType notificationType, NotificationStatus status);
    List<Notification> findByRelatedBookingId(Long bookingId);
    @Query("""
        SELECT n FROM Notification n
        WHERE n.recipientEmail = :email
        AND n.status != 'READ'
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findUnreadNotifications(@Param("email") String email);
    @Query("""
        SELECT n FROM Notification n
        WHERE n.createdAt BETWEEN :startTime AND :endTime
        ORDER BY n.createdAt DESC
    """)
    List<Notification> findByDateRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    @Query("""
        SELECT n FROM Notification n
        WHERE n.status = 'FAILED'
        AND n.createdAt <= :createdBefore
        ORDER BY n.createdAt ASC
    """)
    List<Notification> findOldFailedNotificationsForRetry(@Param("createdBefore") LocalDateTime createdBefore);
}