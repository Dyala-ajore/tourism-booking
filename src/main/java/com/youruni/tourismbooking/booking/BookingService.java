package com.youruni.tourismbooking.booking;
import com.youruni.tourismbooking.common.PagedResponse;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
public interface BookingService {
    BookingDtoResponse createBooking(CreateBookingDtoRequest request, String authenticatedUsername);
    BookingDtoResponse getBookingById(Long id, String authenticatedUsername, String userRole);
    PagedResponse<BookingDtoResponse> getAllBookings(
            String guestEmail,
            BookingStatus status,
            Long roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );
    PagedResponse<BookingDtoResponse> getAllBookingsFiltered(
            String guestEmail,
            BookingStatus status,
            Long roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            String authenticatedUsername,
            String userRole,
            Pageable pageable
    );
    CancelBookingDtoResponse cancelBooking(Long id, String authenticatedUsername, String userRole);
    Booking getBookingEntity(Long id);
    void confirmBooking(Long id);
}