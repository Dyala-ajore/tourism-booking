package com.youruni.tourismbooking.notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    public void sendBookingConfirmation(Long bookingId) {
        logger.info("📧 Sending booking confirmation email for Booking ID: {}", bookingId);
    }
    public void sendBookingCancellation(Long bookingId) {
        logger.info("📧 Sending booking cancellation email for Booking ID: {}", bookingId);
    }
    public void sendPaymentFailure(Long bookingId) {
        logger.warn("⚠️ Sending payment failure notification email for Booking ID: {}", bookingId);
    }
}