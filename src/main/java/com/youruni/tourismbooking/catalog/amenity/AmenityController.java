package com.youruni.tourismbooking.catalog.amenity;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
@RestController
@RequestMapping("/api/amenities")
@Tag(name = "Amenities", description = "Amenity management API")
public class AmenityController {
    private static final Set<String> VALID_SORT_FIELDS = new LinkedHashSet<>(
            Arrays.asList("name", "type", "scope", "createdAt")
    );
    private final AmenityService amenityService;
    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new amenity", description = "Create a new amenity with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Amenity created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate amenity name")
    })
    public ResponseEntity<AmenityDtoResponse> createAmenity(@Valid @RequestBody AmenityDtoRequest request) {
        AmenityDtoResponse response = amenityService.createAmenity(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    @Operation(summary = "Get amenity by ID", description = "Retrieve a specific amenity by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenity found"),
            @ApiResponse(responseCode = "404", description = "Amenity not found")
    })
    public ResponseEntity<AmenityDtoResponse> getAmenityById(@PathVariable Long id) {
        AmenityDtoResponse response = amenityService.getAmenityById(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'GUEST')")
    @Operation(summary = "Get all amenities", description = "Retrieve all amenities with optional filtering and pagination")
    @ApiResponse(responseCode = "200", description = "Amenities retrieved successfully")
    public ResponseEntity<PagedResponse<AmenityDtoResponse>> getAllAmenities(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field (available: name, type, scope, createdAt)", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filter by amenity name (case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by amenity type")
            @RequestParam(required = false) AmenityType type,
            @Parameter(description = "Filter by amenity scope")
            @RequestParam(required = false) AmenityScope scope,
            @Parameter(description = "Filter by active status")
            @RequestParam(required = false) Boolean active) {
        if (!VALID_SORT_FIELDS.contains(sortBy)) {
            sortBy = "name";
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        PagedResponse<AmenityDtoResponse> response = amenityService.getAllAmenities(name, type, scope, active, pageable);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing amenity", description = "Update the details of an existing amenity")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Amenity not found"),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate amenity name")
    })
    public ResponseEntity<AmenityDtoResponse> updateAmenity(
            @PathVariable Long id,
            @Valid @RequestBody AmenityDtoRequest request) {
        AmenityDtoResponse response = amenityService.updateAmenity(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an amenity", description = "Delete an amenity by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Amenity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Amenity not found")
    })
    public ResponseEntity<Void> deleteAmenity(@PathVariable Long id) {
        amenityService.deleteAmenity(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/hotels/{hotelId}/amenities/{amenityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Assign amenity to hotel", description = "Assign an amenity to a hotel")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenity assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel or amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to manage this hotel's amenities"),
            @ApiResponse(responseCode = "400", description = "Amenity scope not compatible with hotel")
    })
    public ResponseEntity<Void> assignAmenityToHotel(
            @PathVariable Long hotelId,
            @PathVariable Long amenityId,
            Authentication authentication) {
        amenityService.assignAmenityToHotel(hotelId, amenityId, authentication.getName());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/hotels/{hotelId}/amenities/{amenityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Remove amenity from hotel", description = "Remove an amenity from a hotel")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Amenity removed successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel or amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to manage this hotel's amenities")
    })
    public ResponseEntity<Void> removeAmenityFromHotel(
            @PathVariable Long hotelId,
            @PathVariable Long amenityId,
            Authentication authentication) {
        amenityService.removeAmenityFromHotel(hotelId, amenityId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/room-types/{roomTypeId}/amenities/{amenityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Assign amenity to room type", description = "Assign an amenity to a room type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Amenity assigned successfully"),
            @ApiResponse(responseCode = "404", description = "Room type or amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to manage this hotel's room types"),
            @ApiResponse(responseCode = "400", description = "Amenity scope not compatible with room type")
    })
    public ResponseEntity<Void> assignAmenityToRoomType(
            @PathVariable Long roomTypeId,
            @PathVariable Long amenityId,
            Authentication authentication) {
        amenityService.assignAmenityToRoomType(roomTypeId, amenityId, authentication.getName());
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/room-types/{roomTypeId}/amenities/{amenityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Remove amenity from room type", description = "Remove an amenity from a room type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Amenity removed successfully"),
            @ApiResponse(responseCode = "404", description = "Room type or amenity not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden: You do not have permission to manage this hotel's room types")
    })
    public ResponseEntity<Void> removeAmenityFromRoomType(
            @PathVariable Long roomTypeId,
            @PathVariable Long amenityId,
            Authentication authentication) {
        amenityService.removeAmenityFromRoomType(roomTypeId, amenityId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}