package com.youruni.tourismbooking.payment;
import com.youruni.tourismbooking.booking.Booking;
import com.youruni.tourismbooking.booking.BookingService;
import com.youruni.tourismbooking.booking.BookingStatus;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.common.ForbiddenException;
import com.youruni.tourismbooking.common.NotFoundException;
import com.youruni.tourismbooking.notification.NotificationService;
import com.youruni.tourismbooking.notification.NotificationType;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);
    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingService bookingService,
                              PaymentMapper paymentMapper,
                              NotificationService notificationService,
                              UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingService = bookingService;
        this.paymentMapper = paymentMapper;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }
    @Override
    public PaymentDtoResponse processPayment(PaymentDtoRequest request, String authenticatedUsername, String userRole) {
        logger.info("Processing payment for booking ID: {}, amount: {}", request.getBookingId(), request.getAmount());
        Booking booking = bookingService.getBookingEntity(request.getBookingId());
        validatePaymentAccess(booking, authenticatedUsername, userRole, "create payment");
        if (booking.getStatus() != BookingStatus.PENDING) {
            logger.warn("Payment processing failed: Booking {} already processed with status {}", booking.getId(), booking.getStatus());
            throw new ConflictException("Booking already processed");
        }
        if (paymentRepository.existsSuccessfulPaymentForBooking(request.getBookingId())) {
            logger.warn("Payment processing failed: Booking {} already has successful payment", booking.getId());
            throw new ConflictException("Booking already paid");
        }
        if (request.getAmount().compareTo(booking.getTotalPrice()) != 0) {
            logger.warn("Payment processing failed: Amount mismatch. Expected: {}, Received: {}", booking.getTotalPrice(), request.getAmount());
            throw new ConflictException(
                    "Payment amount does not match booking total"
            );
        }
        Payment payment = paymentMapper.toEntity(request);
        payment.setBooking(booking);
        payment.setTransactionReference(generateTransactionReference(booking.getId()));
        boolean success = request.getSimulateSuccess() != null
                ? request.getSimulateSuccess()
                : Math.random() > 0.2;
        if (success) {
            payment.setStatus(PaymentStatus.SUCCESS);
            logger.info("Payment successful for booking {}, transaction reference: {}", booking.getId(), payment.getTransactionReference());
            bookingService.confirmBooking(booking.getId());
            notificationService.sendBookingNotification(
                    booking.getId(),
                    NotificationType.BOOKING_CONFIRMATION
            );
            notificationService.sendBookingNotification(
                    booking.getId(),
                    NotificationType.PAYMENT_RECEIVED
            );
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            logger.warn("Payment failed for booking {} (simulated)", booking.getId());
            notificationService.sendBookingNotification(
                    booking.getId(),
                    NotificationType.PAYMENT_FAILED
            );
        }
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponseDto(savedPayment);
    }
    @Override
    public PaymentDtoResponse getPaymentById(Long id, String authenticatedUsername, String userRole) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Payment not found with ID: " + id
                ));
        validatePaymentAccess(payment.getBooking(), authenticatedUsername, userRole, "read payment");
        return paymentMapper.toResponseDto(payment);
    }
    @Override
    public List<PaymentDtoResponse> getPaymentsByBookingId(Long bookingId, String authenticatedUsername, String userRole) {
        Booking booking = bookingService.getBookingEntity(bookingId);
        validatePaymentAccess(booking, authenticatedUsername, userRole, "read payments");
        return paymentRepository.findByBooking_Id(bookingId)
                .stream()
                .map(paymentMapper::toResponseDto)
                .toList();
    }
    @Override
    public PaymentDtoResponse refundPayment(Long paymentId, String authenticatedUsername, String userRole) {
        logger.info("Processing refund for payment ID: {}", paymentId);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        validatePaymentAccess(payment.getBooking(), authenticatedUsername, userRole, "refund payment");
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            logger.warn("Refund failed: Payment {} has status {} (must be SUCCESS)", paymentId, payment.getStatus());
            throw new ConflictException("Only successful payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        logger.info("Refund successfully processed for payment ID: {}, booking ID: {}", paymentId, saved.getBooking().getId());
        return paymentMapper.toResponseDto(saved);
    }
    @Override
    public PaymentDtoResponse refundPaymentByBookingId(Long bookingId, String authenticatedUsername, String userRole) {
        logger.info("Processing refund for booking ID: {}", bookingId);
        Booking booking = bookingService.getBookingEntity(bookingId);
        validatePaymentAccess(booking, authenticatedUsername, userRole, "refund payment");
        Payment payment = paymentRepository
                .findSuccessfulPaymentByBookingId(bookingId)
                .orElseThrow(() -> new NotFoundException("No successful payment found"));
        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        logger.info("Refund successfully processed by booking for booking ID: {}", bookingId);
        return paymentMapper.toResponseDto(saved);
    }
    private void validatePaymentAccess(Booking booking, String authenticatedUsername, String userRole, String action) {
        if ("ADMIN".equals(userRole)) {
            return; 
        }
        if ("GUEST".equals(userRole)) {
            User user = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
            if (!booking.getGuestEmail().equals(user.getEmail())) {
                throw new ForbiddenException("You do not have permission to " + action + " for this booking");
            }
            return;
        }
        if ("MANAGER".equals(userRole)) {
            User manager = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
            Long hotelManagedByUserId = booking.getRoomType().getHotel().getManagedByUserId();
            if (hotelManagedByUserId == null || !hotelManagedByUserId.equals(manager.getId())) {
                throw new ForbiddenException("You do not have permission to " + action + " for this booking");
            }
            return;
        }
        throw new ForbiddenException("You do not have permission to " + action);
    }
    private String generateTransactionReference(Long bookingId) {
        long timestamp = System.currentTimeMillis();
        String randomComponent = String.format("%08x", new java.util.Random().nextInt());
        return String.format("TXN-%d-%d-%s", bookingId, timestamp, randomComponent);
    }
}