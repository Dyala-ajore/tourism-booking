package com.youruni.tourismbooking.catalog.room;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.common.ForbiddenException;
import com.youruni.tourismbooking.common.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
@Service
public class RoomTypeService {
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeMapper roomTypeMapper;
    private final UserRepository userRepository;
    public RoomTypeService(RoomTypeRepository roomTypeRepository,
                           HotelRepository hotelRepository,
                           RoomTypeMapper roomTypeMapper,
                           UserRepository userRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeMapper = roomTypeMapper;
        this.userRepository = userRepository;
    }
    @Transactional
    public RoomType createRoomType(RoomTypeDtoRequest request) {
        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() ->
                        new NotFoundException("Hotel not found with id: " + request.getHotelId()));
        validateRoomTypeOwnership(hotel);
        validateRoomTypeUniqueness(request, null, hotel.getId());
        RoomType roomType = roomTypeMapper.toEntity(request);
        roomType.setHotel(hotel);
        return roomTypeRepository.save(roomType);
    }
    public RoomType getRoomTypeById(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Room type not found with id: " + id));
    }
    public Page<RoomType> getAllRoomTypes(Long hotelId,
                                          String name,
                                          Integer minCapacity,
                                          Double maxPrice,
                                          Boolean active,
                                          Pageable pageable) {
        BigDecimal maxPriceBigDecimal = maxPrice != null ? new BigDecimal(maxPrice) : null;
        return roomTypeRepository.findWithFilters(
                hotelId, name, minCapacity, maxPriceBigDecimal, active, pageable);
    }
    @Transactional
    public RoomType updateRoomType(Long id, RoomTypeDtoRequest request) {
        RoomType roomType = getRoomTypeById(id);
        Long newHotelId = request.getHotelId() != null
                ? request.getHotelId()
                : roomType.getHotel().getId();
        Hotel hotel = hotelRepository.findById(newHotelId)
                .orElseThrow(() ->
                        new NotFoundException("Hotel not found with id: " + newHotelId));
        validateRoomTypeOwnership(hotel);
        validateRoomTypeUniqueness(request, id, hotel.getId());
        if (!hotel.getId().equals(roomType.getHotel().getId())) {
            roomType.setHotel(hotel);
        }
        roomTypeMapper.updateEntity(request, roomType);
        return roomTypeRepository.save(roomType);
    }
    @Transactional
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with id: " + id));
        validateRoomTypeOwnership(roomType.getHotel());
        roomTypeRepository.deleteById(id);
    }
    private void validateRoomTypeUniqueness(RoomTypeDtoRequest request,
                                            Long excludeId,
                                            Long hotelId) {
        if (roomTypeRepository.existsByHotel_IdAndNameIgnoreCase(
                hotelId, request.getName())) {
            if (excludeId != null) {
                Optional<RoomType> existing =
                        roomTypeRepository.findByHotel_IdAndNameIgnoreCase(
                                hotelId, request.getName());
                if (existing.isPresent() &&
                        existing.get().getId().equals(excludeId)) {
                    return;
                }
            }
            throw new ConflictException(
                    "A room type with the same name already exists in this hotel");
        }
    }
    private void validateRoomTypeOwnership(Hotel hotel) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Long currentUserId = getCurrentUserId();
        if (hotel.getManagedByUserId() == null || !hotel.getManagedByUserId().equals(currentUserId)) {
            throw new ForbiddenException(
                    "You do not have permission to manage room types for this hotel. Only the hotel owner or an admin can manage room types."
            );
        }
    }
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("Unable to determine current user");
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new BadRequestException("Unable to determine username from authentication");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Current user not found in database: " + username));
        return user.getId();
    }
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals("ROLE_ADMIN"));
    }
    @Transactional
    public RoomTypeDtoResponse uploadRoomTypeImage(Long id, org.springframework.web.multipart.MultipartFile file,
                                                   com.youruni.tourismbooking.common.FileStorageService fileStorageService) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + id));
        validateRoomTypeOwnership(roomType.getHotel());
        String imagePath = fileStorageService.uploadRoomTypeImage(file);
        roomType.setImagePath(imagePath);
        RoomType updatedRoomType = roomTypeRepository.save(roomType);
        return roomTypeMapper.toResponse(updatedRoomType);
    }
}