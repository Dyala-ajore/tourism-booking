package com.youruni.tourismbooking.catalog.amenity;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
import com.youruni.tourismbooking.common.ForbiddenException;
import com.youruni.tourismbooking.common.NotFoundException;
import com.youruni.tourismbooking.common.PagedResponse;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@Transactional
public class AmenityServiceImpl implements AmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;
    public AmenityServiceImpl(AmenityRepository amenityRepository,
                             AmenityMapper amenityMapper,
                             HotelRepository hotelRepository,
                             RoomTypeRepository roomTypeRepository,
                             UserRepository userRepository) {
        this.amenityRepository = amenityRepository;
        this.amenityMapper = amenityMapper;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.userRepository = userRepository;
    }
    @Override
    public AmenityDtoResponse createAmenity(AmenityDtoRequest request) {
        if (request.getName() != null) {
            request.setName(request.getName().trim());
        }
        validateAmenityUniqueness(request, null);
        Amenity amenity = amenityMapper.toEntity(request);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toResponse(savedAmenity);
    }
    @Override
    @Transactional(readOnly = true)
    public AmenityDtoResponse getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + id));
        return amenityMapper.toResponse(amenity);
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AmenityDtoResponse> getAllAmenities(String name, AmenityType type, AmenityScope scope, Boolean active, Pageable pageable) {
        Specification<Amenity> spec = AmenitySpecification.withFilters(name, type, scope, active);
        Page<Amenity> page = amenityRepository.findAll(spec, pageable);
        return new PagedResponse<>(
                page.getContent().stream()
                        .map(amenityMapper::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    @Override
    public AmenityDtoResponse updateAmenity(Long id, AmenityDtoRequest request) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + id));
        if (request.getName() != null) {
            request.setName(request.getName().trim());
        }
        validateAmenityUniqueness(request, id);
        amenityMapper.updateEntity(request, amenity);
        Amenity updatedAmenity = amenityRepository.save(amenity);
        return amenityMapper.toResponse(updatedAmenity);
    }
    @Override
    public void deleteAmenity(Long id) {
        if (!amenityRepository.existsById(id)) {
            throw new NotFoundException("Amenity not found with ID: " + id);
        }
        amenityRepository.deleteById(id);
    }
    @Override
    public void assignAmenityToHotel(Long hotelId, Long amenityId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + hotelId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        if (amenity.getScope() == AmenityScope.ROOM_TYPE) {
            throw new BadRequestException(
                    "Cannot assign ROOM_TYPE amenity to hotel. Amenity scope is not compatible.");
        }
        hotel.getAmenities().add(amenity);
        hotelRepository.save(hotel);
    }
    @Override
    public void removeAmenityFromHotel(Long hotelId, Long amenityId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + hotelId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        hotel.getAmenities().remove(amenity);
        hotelRepository.save(hotel);
    }
    @Override
    public void assignAmenityToRoomType(Long roomTypeId, Long amenityId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + roomTypeId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        if (amenity.getScope() == AmenityScope.HOTEL) {
            throw new BadRequestException(
                    "Cannot assign HOTEL amenity to room type. Amenity scope is not compatible.");
        }
        roomType.getAmenitySet().add(amenity);
        roomTypeRepository.save(roomType);
    }
    @Override
    public void removeAmenityFromRoomType(Long roomTypeId, Long amenityId) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + roomTypeId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        roomType.getAmenitySet().remove(amenity);
        roomTypeRepository.save(roomType);
    }
    private void validateAmenityUniqueness(AmenityDtoRequest request, Long excludeId) {
        if (amenityRepository.existsByNameIgnoreCase(request.getName())) {
            if (excludeId != null) {
                var existing = amenityRepository.findByNameIgnoreCase(request.getName());
                if (existing.isPresent() && existing.get().getId().equals(excludeId)) {
                    return; 
                }
            }
            throw new ConflictException(
                    "An amenity with the name '" + request.getName() + "' already exists");
        }
    }
    @Override
    public void assignAmenityToHotel(Long hotelId, Long amenityId, String authenticatedUsername) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + hotelId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        validateAmenityOwnership(hotel, authenticatedUsername);
        if (amenity.getScope() == AmenityScope.ROOM_TYPE) {
            throw new BadRequestException(
                    "Cannot assign ROOM_TYPE amenity to hotel. Amenity scope is not compatible.");
        }
        hotel.getAmenities().add(amenity);
        hotelRepository.save(hotel);
    }
    @Override
    public void removeAmenityFromHotel(Long hotelId, Long amenityId, String authenticatedUsername) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + hotelId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        validateAmenityOwnership(hotel, authenticatedUsername);
        hotel.getAmenities().remove(amenity);
        hotelRepository.save(hotel);
    }
    @Override
    public void assignAmenityToRoomType(Long roomTypeId, Long amenityId, String authenticatedUsername) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + roomTypeId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        validateAmenityOwnership(roomType.getHotel(), authenticatedUsername);
        if (amenity.getScope() == AmenityScope.HOTEL) {
            throw new BadRequestException(
                    "Cannot assign HOTEL amenity to room type. Amenity scope is not compatible.");
        }
        roomType.getAmenitySet().add(amenity);
        roomTypeRepository.save(roomType);
    }
    @Override
    public void removeAmenityFromRoomType(Long roomTypeId, Long amenityId, String authenticatedUsername) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException("Room type not found with ID: " + roomTypeId));
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new NotFoundException("Amenity not found with ID: " + amenityId));
        validateAmenityOwnership(roomType.getHotel(), authenticatedUsername);
        roomType.getAmenitySet().remove(amenity);
        roomTypeRepository.save(roomType);
    }
    private void validateAmenityOwnership(Hotel hotel, String authenticatedUsername) {
        User user = userRepository.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new BadRequestException("Current user not found: " + authenticatedUsername));
        boolean isAdmin = user.getRole() != null && user.getRole().toString().equals("ADMIN");
        if (isAdmin) {
            return; 
        }
        if (hotel.getManagedByUserId() == null || !hotel.getManagedByUserId().equals(user.getId())) {
            throw new ForbiddenException(
                    "You do not have permission to manage amenities for this hotel"
            );
        }
    }
}