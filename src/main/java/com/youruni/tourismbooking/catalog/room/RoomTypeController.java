package com.youruni.tourismbooking.catalog.room;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.youruni.tourismbooking.common.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/room-types")
@Tag(name = "Room Type Management", description = "Endpoints for managing room types")
public class RoomTypeController {
    private final RoomTypeService roomTypeService;
    private final RoomTypeMapper roomTypeMapper;
    private final FileStorageService fileStorageService;
    public RoomTypeController(RoomTypeService roomTypeService, RoomTypeMapper roomTypeMapper, 
                              FileStorageService fileStorageService) {
        this.roomTypeService = roomTypeService;
        this.roomTypeMapper = roomTypeMapper;
        this.fileStorageService = fileStorageService;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Create a new room type (Admin/Manager only)")
    public ResponseEntity<RoomTypeDtoResponse> createRoomType(@Valid @RequestBody RoomTypeDtoRequest request) {
        RoomType roomType = roomTypeService.createRoomType(request);
        return new ResponseEntity<>(roomTypeMapper.toResponse(roomType), HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    @Operation(summary = "Get room type by ID")
    public ResponseEntity<RoomTypeDtoResponse> getRoomTypeById(@PathVariable Long id) {
        RoomType roomType = roomTypeService.getRoomTypeById(id);
        return ResponseEntity.ok(roomTypeMapper.toResponse(roomType));
    }
    @GetMapping
    @Operation(summary = "Get all room types with pagination and filtering")
    public ResponseEntity<Page<RoomTypeDtoResponse>> getAllRoomTypes(
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<RoomType> roomTypes = roomTypeService.getAllRoomTypes(
                hotelId, name, minCapacity, maxPrice, active, pageable);
        return ResponseEntity.ok(roomTypes.map(roomTypeMapper::toResponse));
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Update an existing room type (Admin/Manager only)")
    public ResponseEntity<RoomTypeDtoResponse> updateRoomType(
            @PathVariable Long id,
            @Valid @RequestBody RoomTypeDtoRequest request) {
        RoomType roomType = roomTypeService.updateRoomType(id, request);
        return ResponseEntity.ok(roomTypeMapper.toResponse(roomType));
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Delete a room type (Admin/Manager only)")
    public ResponseEntity<Void> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/image")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Upload room type image", description = "Upload an image for a room type (Admin/Manager only)")
    public ResponseEntity<RoomTypeDtoResponse> uploadRoomTypeImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file) {
        RoomTypeDtoResponse roomType = roomTypeService.uploadRoomTypeImage(id, file, fileStorageService);
        return ResponseEntity.ok(roomType);
    }
}