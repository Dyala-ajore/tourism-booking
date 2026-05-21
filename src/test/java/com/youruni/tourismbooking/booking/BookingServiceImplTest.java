package com.youruni.tourismbooking.booking;

import com.youruni.tourismbooking.availabilityPricing.Availability;
import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.availabilityPricing.PricingService;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.common.NotFoundException;
import com.youruni.tourismbooking.notification.NotificationService;
import com.youruni.tourismbooking.payment.PaymentRepository;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private AvailabilityRepository availabilityRepository;
    @Mock private NotificationService notificationService;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PricingService pricingService;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private RoomType roomType;
    private Availability availability;
    private CreateBookingDtoRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setTotalRooms(10);
        roomType.setBasePricePerNight(new BigDecimal("150.00"));
        roomType.setActive(true);

        availability = new Availability();
        availability.setId(1L);
        availability.setRoomTypeId(1L);
        availability.setAvailableRooms(5);
        availability.setPricePerNight(new BigDecimal("150.00"));
        availability.setAvailabilityDate(LocalDate.now().plusDays(1));

        request = new CreateBookingDtoRequest();
        request.setRoomTypeId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(2));
        request.setGuestsCount(2);
        request.setGuestName("Test Guest");
        request.setGuestPhone("0599999999");

        user = new User();
        user.setId(10L);
        user.setUsername("testuser");
        user.setEmail("guest@example.com");
        user.setFullName("Test User");
        user.setRole(UserRole.GUEST);
        user.setEnabled(true);
    }

    @Test
    void createBooking_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDateForUpdate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));
        when(pricingService.applyDayOfWeekMultiplier(any(BigDecimal.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("150.00"));

        Booking savedBooking = createBooking(BookingStatus.PENDING);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingDtoResponse response = new BookingDtoResponse();
        response.setId(1L);
        response.setGuestEmail("guest@example.com");
        response.setStatus(BookingStatus.PENDING);
        response.setTotalPrice(new BigDecimal("150.00"));

        when(bookingMapper.toResponseDto(any(Booking.class))).thenReturn(response);

        BookingDtoResponse result = bookingService.createBooking(request, "testuser");

        assertNotNull(result);
        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("150.00"), result.getTotalPrice());
        verify(bookingRepository).save(any(Booking.class));
        verify(availabilityRepository).save(any(Availability.class));
    }

    @Test
    void createBooking_AuthenticatedUserNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(request, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_RoomTypeNotFound_ThrowsNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(request, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_GuestCountExceedsCapacity_ThrowsBadRequestException() {
        request.setGuestsCount(10);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_NoAvailability_ThrowsBadRequestException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDateForUpdate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request, "testuser"));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_NoRoomsAvailable_ThrowsConflictException() {
        availability.setAvailableRooms(0);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDateForUpdate(eq(1L), any(LocalDate.class)))
                .thenReturn(Optional.of(availability));

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking(request, "testuser"));
    }

    @Test
    void cancelBooking_Success_AsGuestOwner() {
        Booking booking = createBooking(BookingStatus.PENDING);
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        booking.setCheckOutDate(LocalDate.now().plusDays(6));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(availabilityRepository.findByRoomTypeIdAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(availability));
        when(paymentRepository.existsSuccessfulPaymentForBooking(1L)).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        CancelBookingDtoResponse result = bookingService.cancelBooking(1L, "testuser", "GUEST");

        assertNotNull(result);
        assertEquals(BookingStatus.CANCELED, result.getNewStatus());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_AlreadyCanceled_ThrowsConflictException() {
        Booking booking = createBooking(BookingStatus.CANCELED);
        booking.setCheckInDate(LocalDate.now().plusDays(5));
        booking.setCheckOutDate(LocalDate.now().plusDays(6));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(ConflictException.class,
                () -> bookingService.cancelBooking(1L, "testuser", "GUEST"));
    }

    @Test
    void cancelBooking_OnCheckInDate_ThrowsBadRequestException() {
        Booking booking = createBooking(BookingStatus.PENDING);
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> bookingService.cancelBooking(1L, "testuser", "GUEST"));
    }

    @Test
    void getBookingById_NotFound_ThrowsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(99L, "testuser", "GUEST"));
    }

    @Test
    void confirmBooking_Success() {
        Booking booking = createBooking(BookingStatus.PENDING);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.confirmBooking(1L);

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    private Booking createBooking(BookingStatus status) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setGuestEmail("guest@example.com");
        booking.setGuestName("Test Guest");
        booking.setGuestPhone("0599999999");
        booking.setRoomType(roomType);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuestsCount(2);
        booking.setTotalPrice(new BigDecimal("150.00"));
        booking.setStatus(status);
        return booking;
    }
}