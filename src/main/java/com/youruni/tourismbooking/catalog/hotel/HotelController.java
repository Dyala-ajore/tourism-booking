package com.youruni.tourismbooking.catalog.hotel;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.PagedResponse;
import com.youruni.tourismbooking.common.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Set;
@RestController
@RequestMapping("/api/hotels")
@Tag(name = "Hotels", description = "Hotel catalog management API")
public class HotelController {
    private static final Set<String> VALID_SORT_FIELDS = new LinkedHashSet<>(
            Arrays.asList("name", "city", "country", "createdAt")
    );
    private final HotelService hotelService;
    private final FileStorageService fileStorageService;
    public HotelController(HotelService hotelService, FileStorageService fileStorageService) {
        this.hotelService = hotelService;
        this.fileStorageService = fileStorageService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create a new hotel", description = "Create a new hotel with the provided details (Admin/Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hotel created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation (including duplicate hotel or missing manager assignment for ADMIN)"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role")
    })
    public ResponseEntity<HotelDtoResponse> createHotel(
            @Valid @RequestBody HotelDtoRequest request,
            Authentication authentication) {
        String userRole = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
        HotelDtoResponse response = hotelService.createHotel(request, authentication.getName(), userRole);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    @Operation(
            summary = "Get all hotels",
            description = "Retrieve all hotels with optional filtering, sorting, and pagination",
            parameters = {
                    @Parameter(name = "page", description = "Page number (0-indexed)", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "sortBy", description = "Field to sort by (name, city, country, createdAt)", example = "name"),
                    @Parameter(name = "sortDir", description = "Sort direction (asc, desc)", example = "asc"),
                    @Parameter(name = "name", description = "Filter by hotel name (contains, case-insensitive)"),
                    @Parameter(name = "city", description = "Filter by city (exact match)"),
                    @Parameter(name = "country", description = "Filter by country (exact match)")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotels retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid sort field or pagination parameters")
    })
    @SuppressWarnings("null")
    public ResponseEntity<PagedResponse<HotelDtoResponse>> getAllHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country
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
        PagedResponse<HotelDtoResponse> response = hotelService.getAllHotels(name, city, country, pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    @Operation(summary = "Get hotel by ID", description = "Retrieve hotel details by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelDtoResponse> getHotelById(
            @Parameter(description = "Hotel ID", required = true)
            @PathVariable Long id
    ) {
        HotelDtoResponse response = hotelService.getHotelById(id);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update a hotel", description = "Update an existing hotel with new details (Admin can update any hotel; Manager can only update their own hotels)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation (including duplicate hotel)"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role, MANAGER must own the hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelDtoResponse> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelDtoRequest request
    ) {
        HotelDtoResponse response = hotelService.updateHotel(id, request);
        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Partially update a hotel", description = "Partially update an existing hotel with new details (Admin can update any hotel; Manager can only update their own hotels)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role, MANAGER must own the hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelDtoResponse> partialUpdateHotel(
            @PathVariable Long id,
            @RequestBody HotelDtoRequest request
    ) {
        HotelDtoResponse response = hotelService.partialUpdateHotel(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete a hotel", description = "Delete a hotel by its ID (Admin can delete any hotel; Manager can only delete their own hotels)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hotel deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete hotel with associated room types"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role, MANAGER must own the hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Upload hotel image", description = "Upload an image for a hotel (Admin/Manager only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or empty file"),
            @ApiResponse(responseCode = "403", description = "Forbidden: requires ADMIN or MANAGER role, MANAGER must own the hotel"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Object> uploadHotelImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file,
            Authentication authentication) {
        HotelDtoResponse hotel = hotelService.uploadHotelImage(id, file, fileStorageService);
        return ResponseEntity.ok(hotel);
    }
}