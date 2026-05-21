package com.youruni.tourismbooking.booking;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.PagedResponse;
import com.youruni.tourismbooking.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings", description = "Hotel room booking management API")
public class BookingController {
    private static final Set<String> VALID_SORT_FIELDS = new LinkedHashSet<>(
            Arrays.asList("checkInDate", "checkOutDate", "createdAt", "totalPrice", "status", "guestEmail")
    );
    private final BookingService bookingService;
    private final UserRepository userRepository;
    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }
    @PostMapping
    @PreAuthorize("hasRole('GUEST')")
    @Operation(
            summary = "Create a new booking",
            description = "Create a new booking with validation of availability, capacity, and date constraints. Authenticated users can only create bookings for themselves."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation (missing availability, insufficient rooms, invalid dates, guest count exceeds capacity)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: authentication required"),
            @ApiResponse(responseCode = "404", description = "Room type not found")
    })
    public ResponseEntity<BookingDtoResponse> createBooking(
            @Valid @RequestBody CreateBookingDtoRequest request,
            Authentication authentication
    ) {
        BookingDtoResponse response = bookingService.createBooking(request, authentication.getName());
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping("/my-history")
    @PreAuthorize("hasRole('GUEST')")
    @Operation(
            summary = "Get my booking history",
            description = "Retrieve the authenticated user's booking history with pagination and sorting"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking history retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: authentication required")
    })
    @SuppressWarnings("null")
    public ResponseEntity<PagedResponse<BookingDtoResponse>> getMyBookingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) BookingStatus status,
            Authentication authentication
    ) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sort field: '" + sortBy + "'. Allowed fields are: " + VALID_SORT_FIELDS
            );
        }
        Sort.Direction direction;
        try {
            String sortDirUpper = sortDir.toUpperCase();
            direction = Sort.Direction.fromString(sortDirUpper);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid sort direction: '" + sortDir + "'. Use 'asc' or 'desc'.");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page must be >= 0 and size must be > 0");
        }
        String username = authentication.getName();
        String userEmail = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Authenticated user not found"))
                .getEmail();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<BookingDtoResponse> response = bookingService.getAllBookings(
                userEmail, status, null, null, null, pageable
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Get upcoming bookings",
            description = "Retrieve upcoming bookings (check-in dates from today onwards). Managers see only bookings for their hotels; admins see all."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upcoming bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role")
    })
    @SuppressWarnings("null")
    public ResponseEntity<PagedResponse<BookingDtoResponse>> getUpcomingBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Authentication authentication
    ) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sort field: '" + sortBy + "'. Allowed fields are: " + VALID_SORT_FIELDS
            );
        }
        Sort.Direction direction;
        try {
            String sortDirUpper = sortDir.toUpperCase();
            direction = Sort.Direction.fromString(sortDirUpper);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid sort direction: '" + sortDir + "'. Use 'asc' or 'desc'.");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page must be >= 0 and size must be > 0");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        PagedResponse<BookingDtoResponse> response = bookingService.getAllBookingsFiltered(
                null, null, null, LocalDate.now(), null, authentication.getName(), userRole, pageable
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Get all bookings",
            description = "Retrieve all bookings with optional filtering, sorting, and pagination. Managers see only bookings for their hotels; admins see all.",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "sortBy", description = "Field to sort by (checkInDate, checkOutDate, createdAt, totalPrice, status, guestEmail)", example = "checkInDate"),
                    @Parameter(name = "sortDir", description = "Sort direction (asc, desc)", example = "asc"),
                    @Parameter(name = "guestEmail", description = "Filter by guest email"),
                    @Parameter(name = "status", description = "Filter by booking status (PENDING, CONFIRMED, CANCELED)"),
                    @Parameter(name = "roomTypeId", description = "Filter by room type ID"),
                    @Parameter(name = "startDate", description = "Filter by minimum check-in date (inclusive)"),
                    @Parameter(name = "endDate", description = "Filter by maximum check-out date (inclusive)")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid sort field, pagination parameters, date format, or invalid date range"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role")
    })
    @SuppressWarnings("null")
    public ResponseEntity<PagedResponse<BookingDtoResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String guestEmail,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Authentication authentication
    ) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sort field: '" + sortBy + "'. Allowed fields are: " + VALID_SORT_FIELDS
            );
        }
        Sort.Direction direction;
        try {
            String sortDirUpper = sortDir.toUpperCase();
            direction = Sort.Direction.fromString(sortDirUpper);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid sort direction: '" + sortDir + "'. Use 'asc' or 'desc'.");
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page must be >= 0 and size must be > 0");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        PagedResponse<BookingDtoResponse> response = bookingService.getAllBookingsFiltered(
                guestEmail, status, roomTypeId, startDate, endDate, authentication.getName(), userRole, pageable
        );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Get booking by ID", description = "Retrieve booking details by its ID. Guests can only access their own bookings; managers and admins can access all bookings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: you do not have permission to access this booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<BookingDtoResponse> getBookingById(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        BookingDtoResponse response = bookingService.getBookingById(id, authentication.getName(), userRole);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('GUEST') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(
            summary = "Cancel a booking",
            description = "Cancel an existing booking and restore availability (only allowed before check-in date). Guests can only cancel their own bookings; managers and admins can cancel any booking."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking canceled successfully"),
            @ApiResponse(responseCode = "400", description = "Business rule violation (already canceled, check-in date passed, or on check-in date)"),
            @ApiResponse(responseCode = "403", description = "Forbidden: you do not have permission to cancel this booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<CancelBookingDtoResponse> cancelBooking(
            @Parameter(description = "Booking ID", required = true)
            @PathVariable Long id,
            Authentication authentication
    ) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        CancelBookingDtoResponse response = bookingService.cancelBooking(id, authentication.getName(), userRole);
        return ResponseEntity.ok(response);
    }
}