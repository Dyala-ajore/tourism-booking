package com.youruni.tourismbooking.payment;

import com.youruni.tourismbooking.booking.Booking;
import com.youruni.tourismbooking.booking.BookingService;
import com.youruni.tourismbooking.booking.BookingStatus;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.notification.NotificationService;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // ⭐ يمنع مشاكل Mockito
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingService bookingService;
    @Mock private PaymentMapper paymentMapper;
    @Mock private NotificationService notificationService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Booking booking;
    private PaymentDtoRequest request;
    private User guestUser;

    @BeforeEach
    void setUp() {

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Palace Hotel");

        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setHotel(hotel);

        booking = new Booking();
        booking.setId(1L);
        booking.setGuestEmail("guest@example.com");
        booking.setRoomType(roomType);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100.00));
        booking.setStatus(BookingStatus.PENDING);

        request = new PaymentDtoRequest();
        request.setBookingId(1L);
        request.setAmount(BigDecimal.valueOf(100.00));
        request.setPaymentMethod("CREDIT_CARD");
        request.setSimulateSuccess(true);

        guestUser = new User();
        guestUser.setId(10L);
        guestUser.setUsername("testuser");
        guestUser.setEmail("guest@example.com");
    }

    @Test
    void processPayment_BookingNotPending_ThrowsConflictException() {

        booking.setStatus(BookingStatus.CONFIRMED);

        when(bookingService.getBookingEntity(1L)).thenReturn(booking);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(guestUser));

        assertThrows(ConflictException.class,
                () -> paymentService.processPayment(request, "testuser", "GUEST"));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_AlreadyPaid_ThrowsConflictException() {

        when(bookingService.getBookingEntity(1L)).thenReturn(booking);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(guestUser));
        when(paymentRepository.existsSuccessfulPaymentForBooking(1L)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> paymentService.processPayment(request, "testuser", "GUEST"));

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_Success() {

        when(bookingService.getBookingEntity(1L)).thenReturn(booking);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(guestUser));
        when(paymentRepository.existsSuccessfulPaymentForBooking(1L)).thenReturn(false);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(any(PaymentDtoRequest.class))).thenReturn(payment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDtoResponse response = new PaymentDtoResponse();
        response.setId(1L);
        response.setBookingId(1L);
        response.setStatus(PaymentStatus.SUCCESS);

        when(paymentMapper.toResponseDto(any(Payment.class))).thenReturn(response);

        PaymentDtoResponse result =
                paymentService.processPayment(request, "testuser", "GUEST");

        assertNotNull(result);
        assertEquals(1L, result.getBookingId());
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());

        verify(paymentRepository).save(any(Payment.class));
        verify(bookingService).confirmBooking(1L);
    }

    @Test
    void processPayment_TransactionReferenceGenerated() {

        when(bookingService.getBookingEntity(1L)).thenReturn(booking);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(guestUser));
        when(paymentRepository.existsSuccessfulPaymentForBooking(1L)).thenReturn(false);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(BigDecimal.valueOf(100.00));
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(any(PaymentDtoRequest.class))).thenReturn(payment);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);

            assertNotNull(p.getTransactionReference());
            assertTrue(p.getTransactionReference().startsWith("TXN-"));

            return p;
        });

        when(paymentMapper.toResponseDto(any())).thenReturn(new PaymentDtoResponse());

        paymentService.processPayment(request, "testuser", "GUEST");
    }
}