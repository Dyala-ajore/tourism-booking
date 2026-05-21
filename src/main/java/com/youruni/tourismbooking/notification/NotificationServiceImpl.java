package com.youruni.tourismbooking.notification;
import com.youruni.tourismbooking.booking.Booking;
import com.youruni.tourismbooking.booking.BookingRepository;
import com.youruni.tourismbooking.common.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final BookingRepository bookingRepository;
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   NotificationMapper notificationMapper,
                                   EmailService emailService,
                                   BookingRepository bookingRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.emailService = emailService;
        this.bookingRepository = bookingRepository;
    }
    @Override
    public NotificationDtoResponse sendNotification(NotificationDtoRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        logger.info("Notification sent to {} with type {}", notification.getRecipientEmail(), request.getNotificationType());
        return notificationMapper.toResponseDto(saved);
    }
    @Override
    public void sendBookingNotification(Long bookingId, NotificationType type) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));
        Notification notification = new Notification();
        notification.setRecipientEmail(booking.getGuestEmail());
        notification.setSubject(type.name());
        notification.setMessage("Notification for booking " + bookingId);
        notification.setNotificationType(type);
        notification.setRelatedBookingId(bookingId);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        logger.info("Booking notification sent for Booking ID: {} with type: {}", bookingId, type);
        switch (type) {
            case BOOKING_CONFIRMATION ->
                    emailService.sendBookingConfirmation(bookingId);
            case BOOKING_CANCELLED ->
                    emailService.sendBookingCancellation(bookingId);
            case PAYMENT_FAILED ->
                    emailService.sendPaymentFailure(bookingId);
            default ->
                    logger.debug("Notification type {} handled without specific email action", type);
        }
    }
    @Override
    public NotificationDtoResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        return notificationMapper.toResponseDto(notification);
    }
    @Override
    public List<NotificationDtoResponse> getNotificationsByBookingId(Long bookingId) {
        return notificationRepository.findByRelatedBookingId(bookingId)
                .stream()
                .map(notificationMapper::toResponseDto)
                .toList();
    }
}