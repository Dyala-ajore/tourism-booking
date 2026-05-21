package com.youruni.tourismbooking.catalog.hotel;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@Transactional
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;
    public HotelServiceImpl(HotelRepository hotelRepository,
                            HotelMapper hotelMapper,
                            RoomTypeRepository roomTypeRepository,
                            UserRepository userRepository) {
        this.hotelRepository = hotelRepository;
        this.hotelMapper = hotelMapper;
        this.roomTypeRepository = roomTypeRepository;
        this.userRepository = userRepository;
    }
    @Override
    @SuppressWarnings("null")
    public HotelDtoResponse createHotel(HotelDtoRequest request) {
        return createHotel(request, getCurrentUsername(), getCurrentUserRole());
    }
    @Override
    @SuppressWarnings("null")
    public HotelDtoResponse createHotel(HotelDtoRequest request, String authenticatedUsername, String userRole) {
        normalizeHotelRequest(request);
        validateHotelUniqueness(request, null);
        Hotel hotel = hotelMapper.toEntity(request);
        if ("ADMIN".equals(userRole)) {
            if (request.getManagedByUserId() == null) {
                throw new BadRequestException("Admin users must specify managedByUserId when creating a hotel");
            }
            User manager = userRepository.findById(request.getManagedByUserId())
                    .orElseThrow(() -> new NotFoundException("Manager user not found with ID: " + request.getManagedByUserId()));
            if (manager.getRole() == null || !manager.getRole().toString().equals("MANAGER")) {
                throw new BadRequestException("Specified user is not a MANAGER. Only MANAGER users can be assigned as hotel managers.");
            }
            hotel.setManagedByUserId(request.getManagedByUserId());
        } else if ("MANAGER".equals(userRole)) {
            Long currentUserId = getCurrentUserId();
            hotel.setManagedByUserId(currentUserId);
        }
        Hotel savedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponseDto(savedHotel);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public HotelDtoResponse getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + id));
        return hotelMapper.toResponseDto(hotel);
    }
    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PagedResponse<HotelDtoResponse> getAllHotels(String name, String city, String country, Pageable pageable) {
        Specification<Hotel> spec = HotelSpecification.withFilters(name, city, country);
        Page<Hotel> page = hotelRepository.findAll(spec, pageable);
        return new PagedResponse<>(
                page.getContent().stream()
                        .map(hotelMapper::toResponseDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    @Override
    @SuppressWarnings("null")
    public HotelDtoResponse updateHotel(Long id, HotelDtoRequest request) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + id));
        validateHotelOwnership(existingHotel);
        normalizeHotelRequest(request);
        validateHotelUniqueness(request, id);
        hotelMapper.updateEntityFromDto(request, existingHotel);
        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toResponseDto(updatedHotel);
    }
    @Override
    @SuppressWarnings("null")
    public HotelDtoResponse partialUpdateHotel(Long id, HotelDtoRequest request) {
        Hotel existingHotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + id));
        validateHotelOwnership(existingHotel);
        normalizeHotelRequest(request);
        if (request.getName() != null && !request.getName().isBlank()) {
            existingHotel.setName(request.getName());
        }
        if (request.getCity() != null && !request.getCity().isBlank()) {
            existingHotel.setCity(request.getCity());
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            existingHotel.setCountry(request.getCountry());
        }
        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            existingHotel.setAddress(request.getAddress());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            existingHotel.setDescription(request.getDescription());
        }
        validateHotelUniqueness(request, id);
        Hotel updatedHotel = hotelRepository.save(existingHotel);
        return hotelMapper.toResponseDto(updatedHotel);
    }
    @Override
    @SuppressWarnings("null")
    public void deleteHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + id));
        validateHotelOwnership(hotel);
        List<RoomType> roomTypes = roomTypeRepository.findByHotel_Id(id);
        if (!roomTypes.isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete hotel: it still has " + roomTypes.size() + " room type(s) associated with it. "
                            + "Please delete all room types first."
            );
        }
        hotelRepository.delete(hotel);
    }
    private void validateHotelUniqueness(HotelDtoRequest request, Long excludeHotelId) {
        String nameTrimmed = request.getName().trim();
        String cityTrimmed = request.getCity().trim();
        String countryTrimmed = request.getCountry().trim();
        String addressTrimmed = request.getAddress().trim();
        if (hotelRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                nameTrimmed,
                cityTrimmed,
                countryTrimmed,
                addressTrimmed)) {
            if (excludeHotelId != null) {
                Hotel existingHotel = hotelRepository.findByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                        nameTrimmed,
                        cityTrimmed,
                        countryTrimmed,
                        addressTrimmed
                ).orElse(null);
                if (existingHotel != null && existingHotel.getId().equals(excludeHotelId)) {
                    return;
                }
            }
            throw new ConflictException(
                    "A hotel with the same name, city, country, and address already exists."
            );
        }
    }
    private void normalizeHotelRequest(HotelDtoRequest request) {
        if (request.getName() != null) {
            request.setName(request.getName().trim());
        }
        if (request.getCity() != null) {
            request.setCity(request.getCity().trim());
        }
        if (request.getCountry() != null) {
            request.setCountry(request.getCountry().trim());
        }
        if (request.getAddress() != null) {
            request.setAddress(request.getAddress().trim());
        }
        if (request.getDescription() != null) {
            request.setDescription(request.getDescription().trim());
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
    private void validateHotelOwnership(Hotel hotel) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Long currentUserId = getCurrentUserId();
        if (hotel.getManagedByUserId() == null || !hotel.getManagedByUserId().equals(currentUserId)) {
            throw new ForbiddenException(
                    "You do not have permission to modify this hotel. Only the hotel owner or an admin can modify it."
            );
        }
    }
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BadRequestException("Unable to determine current user");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        } else {
            throw new BadRequestException("Unable to determine username from authentication");
        }
    }
    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "GUEST";
        }
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("GUEST");
    }
    @Override
    @SuppressWarnings("null")
    public HotelDtoResponse uploadHotelImage(Long id, org.springframework.web.multipart.MultipartFile file, 
                                              com.youruni.tourismbooking.common.FileStorageService fileStorageService) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with ID: " + id));
        validateHotelOwnership(hotel);
        String imagePath = fileStorageService.uploadHotelImage(file);
        hotel.setImagePath(imagePath);
        Hotel updatedHotel = hotelRepository.save(hotel);
        return hotelMapper.toResponseDto(updatedHotel);
    }
}