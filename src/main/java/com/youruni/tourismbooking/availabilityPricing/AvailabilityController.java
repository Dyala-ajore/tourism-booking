package com.youruni.tourismbooking.availabilityPricing;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
@RestController
@RequestMapping("/api/availability")
@Tag(name = "Availability", description = "Availability and pricing management API")
public class AvailabilityController {
    private static final Set<String> VALID_SORT_FIELDS = new LinkedHashSet<>(
            Arrays.asList("availabilityDate", "pricePerNight", "availableRooms", "roomTypeId")
    );
    private final AvailabilityService availabilityService;
    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create availability record (Admin/Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Availability created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business logic violation"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    public ResponseEntity<AvailabilityDtoResponse> createAvailability(
            @Valid @RequestBody AvailabilityDtoRequest request,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        AvailabilityDtoResponse response = availabilityService.createAvailability(request, authentication.getName(), userRole);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    public ResponseEntity<PagedResponse<AvailabilityDtoResponse>> getAllAvailability(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "availabilityDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) LocalDate availabilityDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate startDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean onlyAvailable
    ) {
        if (availabilityDate != null && (startDate != null || endDate != null)) {
            throw new BadRequestException(
                    "Cannot use availabilityDate with startDate/endDate together"
            );
        }
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException(
                    "Invalid sort field: '" + sortBy + "'. Allowed fields are: " + VALID_SORT_FIELDS
            );
        }
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid sort direction: '" + sortDir + "'. Use 'asc' or 'desc'."
            );
        }
        if (page < 0 || size <= 0) {
            throw new BadRequestException("Page must be >= 0 and size must be > 0");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new BadRequestException("Min price cannot be greater than max price");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<AvailabilityDtoResponse> response =
                availabilityService.getAllAvailability(
                        roomTypeId,
                        availabilityDate,
                        startDate,
                        endDate,
                        minPrice,
                        maxPrice,
                        onlyAvailable,
                        pageable
                );
        return ResponseEntity.ok(response);
    }
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    @Operation(summary = "Check availability for a date range and guest count")
    public ResponseEntity<AvailabilityCheckResponse> checkAvailability(
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam int guests) {
        AvailabilityCheckResponse response = availabilityService.checkAvailability(
                roomTypeId, checkIn, checkOut, guests);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    public ResponseEntity<AvailabilityDtoResponse> getAvailabilityById(
            @PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getAvailabilityById(id));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update availability record (Admin/Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Availability updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business logic violation"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions")
    })
    public ResponseEntity<AvailabilityDtoResponse> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityDtoRequest request,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        return ResponseEntity.ok(
                availabilityService.updateAvailability(id, request, authentication.getName(), userRole)
        );
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete availability record (Admin/Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Availability deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden: insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    public ResponseEntity<Void> deleteAvailability(
            @PathVariable Long id,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        availabilityService.deleteAvailability(id, authentication.getName(), userRole);
        return ResponseEntity.noContent().build();
    }
}