package com.youruni.tourismbooking.booking;
import com.youruni.tourismbooking.availabilityPricing.Availability;
import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.availabilityPricing.PricingService;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.common.ForbiddenException;
import com.youruni.tourismbooking.common.NotFoundException;
import com.youruni.tourismbooking.common.PagedResponse;
import com.youruni.tourismbooking.notification.NotificationService;
import com.youruni.tourismbooking.notification.NotificationType;
import com.youruni.tourismbooking.payment.PaymentRepository;
import com.youruni.tourismbooking.payment.PaymentStatus;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
public class BookingServiceImpl implements BookingService {
    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final RoomTypeRepository roomTypeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;
    private final PricingService pricingService;
    private final UserRepository userRepository;
    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingMapper bookingMapper,
            RoomTypeRepository roomTypeRepository,
            AvailabilityRepository availabilityRepository,
            NotificationService notificationService,
            PaymentRepository paymentRepository,
            PricingService pricingService,
            UserRepository userRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.roomTypeRepository = roomTypeRepository;
        this.availabilityRepository = availabilityRepository;
        this.notificationService = notificationService;
        this.paymentRepository = paymentRepository;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
    }
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    private BookingDtoResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
        return bookingMapper.toResponseDto(booking);
    }
    @Override
    @Transactional(readOnly = true)
    public Booking getBookingEntity(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PagedResponse<BookingDtoResponse> getAllBookings(
            String guestEmail,
            BookingStatus status,
            Long roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        Specification<Booking> spec = BookingSpecification.withFilters(
                guestEmail, status, roomTypeId, startDate, endDate
        );
        Page<Booking> page = bookingRepository.findAll(spec, pageable);
        return new PagedResponse<>(
                page.getContent().stream().map(bookingMapper::toResponseDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PagedResponse<BookingDtoResponse> getAllBookingsFiltered(
            String guestEmail,
            BookingStatus status,
            Long roomTypeId,
            LocalDate startDate,
            LocalDate endDate,
            String authenticatedUsername,
            String userRole,
            Pageable pageable
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if ("ADMIN".equals(userRole)) {
            Specification<Booking> spec = BookingSpecification.withFilters(
                    guestEmail, status, roomTypeId, startDate, endDate
            );
            Page<Booking> page = bookingRepository.findAll(spec, pageable);
            return new PagedResponse<>(
                    page.getContent().stream().map(bookingMapper::toResponseDto).toList(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        } else if ("MANAGER".equals(userRole)) {
            User manager = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
            Specification<Booking> spec = BookingSpecification.withFilters(
                    guestEmail, status, roomTypeId, startDate, endDate
            ).and(BookingSpecification.byManagerHotels(manager.getId()));
            Page<Booking> page = bookingRepository.findAll(spec, pageable);
            return new PagedResponse<>(
                    page.getContent().stream().map(bookingMapper::toResponseDto).toList(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
        } else {
            throw new ForbiddenException("Guests cannot access the general bookings list");
        }
    }
@SuppressWarnings("null")
    private CancelBookingDtoResponse cancelBooking(Long id) {
        logger.info("Attempting to cancel booking ID: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
        if (booking.getStatus() == BookingStatus.CANCELED) {
            logger.warn("Booking {} already canceled", id);
            throw new ConflictException("Booking is already canceled");
        }
        if (!booking.getCheckInDate().isAfter(LocalDate.now())) {
            logger.warn(
                    "Booking {} cannot be canceled: check-in date {} is not in the future",
                    id,
                    booking.getCheckInDate()
            );
            throw new BadRequestException("Booking cannot be canceled on or after the check-in date");
        }
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepository.save(booking);
        logger.info("Booking {} status set to CANCELED", id);
        List<Availability> availabilityRecords = availabilityRepository.findByRoomTypeIdAndDateRange(
                booking.getRoomType().getId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate().minusDays(1)
        );
        updateAvailabilityOnBookingCancel(availabilityRecords);
        logger.info("Availability restored for {} nights for booking {}", availabilityRecords.size(), id);
        if (paymentRepository.existsSuccessfulPaymentForBooking(booking.getId())) {
            paymentRepository.findSuccessfulPaymentByBookingId(booking.getId())
                    .ifPresent(payment -> {
                        payment.setStatus(PaymentStatus.REFUNDED);
                        paymentRepository.save(payment);
                        logger.info("Refund processed for booking {}", id);
                    });
        }
        notificationService.sendBookingNotification(
                booking.getId(),
                NotificationType.BOOKING_CANCELLED
        );
        logger.info("Cancellation notification sent for booking {}", id);
        CancelBookingDtoResponse response = new CancelBookingDtoResponse();
        response.setBookingId(booking.getId());
        response.setNewStatus(booking.getStatus());
        response.setGuestEmail(booking.getGuestEmail());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setMessage("Booking cancelled successfully, availability restored, and refund processed if applicable");
        return response;
    }
    @Override
    @SuppressWarnings("null")
    public BookingDtoResponse createBooking(CreateBookingDtoRequest request, String authenticatedUsername) {
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
        logger.info(
                "Creating booking: room type ID={}, guest email={}, check-in={}, check-out={}, guests={}",
                request.getRoomTypeId(),
                user.getEmail(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getGuestsCount()
        );
        validateBookingRequest(request);
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + request.getRoomTypeId()));
        if (request.getGuestsCount() > roomType.getCapacity()) {
            logger.warn(
                    "Booking creation failed: Guest count {} exceeds room capacity {}",
                    request.getGuestsCount(),
                    roomType.getCapacity()
            );
            throw new BadRequestException(
                    "Guest count (" + request.getGuestsCount() + ") exceeds room capacity (" + roomType.getCapacity() + ")"
            );
        }
        List<Availability> availabilityRecords = validateAndRetrieveAvailability(
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );
        BigDecimal totalPrice = calculateTotalPrice(availabilityRecords, request.getCheckInDate());
        Booking booking = new Booking();
        booking.setRoomType(roomType);
        booking.setGuestEmail(user.getEmail());
        booking.setGuestName(request.getGuestName());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuestsCount(request.getGuestsCount());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        Booking savedBooking = bookingRepository.save(booking);
        logger.info("Booking created successfully: booking ID={}, total price={}", savedBooking.getId(), totalPrice);
        updateAvailabilityOnBookingCreate(availabilityRecords);
        return bookingMapper.toResponseDto(savedBooking);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public BookingDtoResponse getBookingById(Long id, String authenticatedUsername, String userRole) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
        if (!canAccessBooking(booking, authenticatedUsername, userRole)) {
            logger.warn("User {} attempted unauthorized access to booking {}", authenticatedUsername, id);
            throw new ForbiddenException("You do not have permission to access this booking");
        }
        return bookingMapper.toResponseDto(booking);
    }
    @Override
    @SuppressWarnings("null")
    public CancelBookingDtoResponse cancelBooking(Long id, String authenticatedUsername, String userRole) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
        if (!canAccessBooking(booking, authenticatedUsername, userRole)) {
            logger.warn("User {} attempted unauthorized cancellation of booking {}", authenticatedUsername, id);
            throw new ForbiddenException("You do not have permission to cancel this booking");
        }
        return cancelBooking(id);
    }
    private boolean canAccessBooking(Booking booking, String authenticatedUsername, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        if ("MANAGER".equals(userRole)) {
            User manager = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
            Long hotelId = booking.getRoomType().getHotel().getId();
            Long hotelManagedByUserId = booking.getRoomType().getHotel().getManagedByUserId();
            return hotelManagedByUserId != null && hotelManagedByUserId.equals(manager.getId());
        }
        if ("GUEST".equals(userRole)) {
            User user = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new NotFoundException("Authenticated user not found: " + authenticatedUsername));
            return booking.getGuestEmail().equals(user.getEmail());
        }
        return false;
    }
    private void validateBookingRequest(CreateBookingDtoRequest request) {
        if (!request.getCheckInDate().isBefore(request.getCheckOutDate())) {
            throw new BadRequestException("Check-in date must be strictly before check-out date");
        }
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Check-in date cannot be in the past");
        }
    }
    private List<Availability> validateAndRetrieveAvailability(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        List<Availability> availabilityRecords = new ArrayList<>();
        LocalDate currentDate = checkIn;
        int nightCount = 0;
        while (currentDate.isBefore(checkOut)) {
            final LocalDate dateForError = currentDate;
            Availability availability = availabilityRepository
                    .findByRoomTypeIdAndAvailabilityDateForUpdate(roomTypeId, currentDate)
                    .orElseThrow(() -> new BadRequestException(
                            "No availability record found for room type on " + dateForError + ". Booking cannot be created."
                    ));
            if (availability.getAvailableRooms() <= 0) {
                throw new ConflictException(
                        "No available rooms for room type on " + currentDate + ". Booking cannot be created."
                );
            }
            availabilityRecords.add(availability);
            currentDate = currentDate.plusDays(1);
            nightCount++;
        }
        if (nightCount == 0) {
            throw new BadRequestException("Booking must span at least one night");
        }
        return availabilityRecords;
    }
    @Override
    public void confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + id));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Only pending bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }
    private BigDecimal calculateTotalPrice(List<Availability> availabilityRecords, LocalDate checkInDate) {
        BigDecimal total = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;
        for (Availability availability : availabilityRecords) {
            BigDecimal nightPrice = pricingService.applyDayOfWeekMultiplier(
                    availability.getPricePerNight(),
                    currentDate
            );
            total = total.add(nightPrice);
            currentDate = currentDate.plusDays(1);
        }
        return total;
    }
    private void updateAvailabilityOnBookingCreate(List<Availability> availabilityRecords) {
        for (Availability availability : availabilityRecords) {
            availability.setAvailableRooms(availability.getAvailableRooms() - 1);
            availabilityRepository.save(availability);
        }
    }
    private void updateAvailabilityOnBookingCancel(List<Availability> availabilityRecords) {
        for (Availability availability : availabilityRecords) {
            availability.setAvailableRooms(availability.getAvailableRooms() + 1);
            availabilityRepository.save(availability);
        }
    }
}