package com.youruni.tourismbooking.availabilityPricing;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
@Service
@Transactional
public class AvailabilityServiceImpl implements AvailabilityService {
    private final AvailabilityRepository availabilityRepository;
    private final AvailabilityMapper availabilityMapper;
    private final RoomTypeRepository roomTypeRepository;
    private final PricingService pricingService;
    private final UserRepository userRepository;
    public AvailabilityServiceImpl(AvailabilityRepository availabilityRepository,
                                   AvailabilityMapper availabilityMapper,
                                   RoomTypeRepository roomTypeRepository,
                                   PricingService pricingService,
                                   UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.availabilityMapper = availabilityMapper;
        this.roomTypeRepository = roomTypeRepository;
        this.pricingService = pricingService;
        this.userRepository = userRepository;
    }
    @Override
    public AvailabilityDtoResponse createAvailability(AvailabilityDtoRequest request, String authenticatedUsername, String userRole) {
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException(
                        "Room type not found with ID: " + request.getRoomTypeId()));
        validateAvailabilityOwnership(roomType, userRole, authenticatedUsername);
        if (request.getAvailabilityDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Availability date cannot be in the past");
        }
        if (request.getAvailableRooms() > roomType.getTotalRooms()) {
            throw new BadRequestException(
                    "Available rooms (" + request.getAvailableRooms() +
                            ") cannot exceed total rooms (" + roomType.getTotalRooms() + ")"
            );
        }
        validateAvailabilityUniqueness(request, null);
        Availability availability = availabilityMapper.toEntity(request);
        Availability saved = availabilityRepository.save(availability);
        return availabilityMapper.toResponseDto(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public AvailabilityDtoResponse getAvailabilityById(Long id) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Availability record not found with ID: " + id));
        return availabilityMapper.toResponseDto(availability);
    }
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AvailabilityDtoResponse> getAllAvailability(
            Long roomTypeId,
            LocalDate availabilityDate,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onlyAvailable,
            Pageable pageable) {
        Specification<Availability> spec = AvailabilitySpecification.withFilters(
                roomTypeId, availabilityDate, startDate, endDate,
                minPrice, maxPrice, onlyAvailable
        );
        Page<Availability> page = availabilityRepository.findAll(spec, pageable);
        return new PagedResponse<>(
                page.getContent().stream()
                        .map(availabilityMapper::toResponseDto)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
    @Override
    public AvailabilityDtoResponse updateAvailability(Long id, AvailabilityDtoRequest request, String authenticatedUsername, String userRole) {
        Availability existing = availabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Availability record not found with ID: " + id));
        Long roomTypeId = request.getRoomTypeId() != null
                ? request.getRoomTypeId()
                : existing.getRoomTypeId();
        LocalDate availabilityDate = request.getAvailabilityDate() != null
                ? request.getAvailabilityDate()
                : existing.getAvailabilityDate();
        Integer availableRooms = request.getAvailableRooms() != null
                ? request.getAvailableRooms()
                : existing.getAvailableRooms();
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException(
                        "Room type not found with ID: " + roomTypeId));
        validateAvailabilityOwnership(roomType, userRole, authenticatedUsername);
        if (availabilityDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Availability date cannot be in the past");
        }
        if (availableRooms > roomType.getTotalRooms()) {
            throw new BadRequestException(
                    "Available rooms (" + availableRooms +
                            ") cannot exceed total rooms (" + roomType.getTotalRooms() + ")"
            );
        }
        validateAvailabilityUniquenessForUpdate(roomTypeId, availabilityDate, id);
        availabilityMapper.updateEntityFromDto(request, existing);
        Availability updated = availabilityRepository.save(existing);
        return availabilityMapper.toResponseDto(updated);
    }
    @Override
    public void deleteAvailability(Long id, String authenticatedUsername, String userRole) {
        Availability availability = availabilityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Availability record not found with ID: " + id));
        RoomType roomType = roomTypeRepository.findById(availability.getRoomTypeId())
                .orElseThrow(() -> new NotFoundException(
                        "Room type not found with ID: " + availability.getRoomTypeId()));
        validateAvailabilityOwnership(roomType, userRole, authenticatedUsername);
        availabilityRepository.deleteById(id);
    }
    @Override
    public AvailabilityCheckResponse checkAvailability(
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guests) {
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new BadRequestException("Invalid date range");
        }
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new NotFoundException(
                        "Room type not found with ID: " + roomTypeId));
        if (guests > roomType.getCapacity()) {
            return new AvailabilityCheckResponse(false, BigDecimal.ZERO, 0);
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDate currentDate = checkIn;
        int nightCount = 0;
        while (currentDate.isBefore(checkOut)) {
            var availability = availabilityRepository
                    .findByRoomTypeIdAndAvailabilityDate(roomTypeId, currentDate)
                    .orElse(null);
            if (availability == null) {
                return new AvailabilityCheckResponse(false, BigDecimal.ZERO, 0);
            }
            if (availability.getAvailableRooms() <= 0) {
                return new AvailabilityCheckResponse(false, BigDecimal.ZERO, 0);
            }
            if (availability.getPricePerNight() == null) {
                return new AvailabilityCheckResponse(false, BigDecimal.ZERO, 0);
            }
            BigDecimal nightPrice = pricingService.applyDayOfWeekMultiplier(
                    availability.getPricePerNight(),
                    currentDate
            );
            totalPrice = totalPrice.add(nightPrice);
            currentDate = currentDate.plusDays(1);
            nightCount++;
        }
        if (nightCount == 0) {
            return new AvailabilityCheckResponse(false, BigDecimal.ZERO, 0);
        }
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return new AvailabilityCheckResponse(true, totalPrice, nights);
    }
    private void validateAvailabilityOwnership(RoomType roomType, String userRole, String authenticatedUsername) {
        if ("ADMIN".equals(userRole)) {
            return; 
        }
        if ("MANAGER".equals(userRole)) {
            User manager = userRepository.findByUsername(authenticatedUsername)
                    .orElseThrow(() -> new BadRequestException("Current user not found in database: " + authenticatedUsername));
            Long hotelManagedByUserId = roomType.getHotel().getManagedByUserId();
            if (hotelManagedByUserId == null || !hotelManagedByUserId.equals(manager.getId())) {
                throw new ForbiddenException("You do not have permission to manage availability for this hotel");
            }
            return;
        }
        throw new ForbiddenException("You do not have permission to manage availability");
    }
    private void validateAvailabilityUniqueness(
            AvailabilityDtoRequest request,
            Long excludeId) {
        Optional<Availability> existing = availabilityRepository
                .findByRoomTypeIdAndAvailabilityDate(
                        request.getRoomTypeId(),
                        request.getAvailabilityDate()
                );
        if (existing.isPresent()) {
            if (excludeId != null &&
                    existing.get().getId().equals(excludeId)) {
                return;
            }
            throw new ConflictException(
                    "Availability already exists for this room type and date");
        }
    }
    private void validateAvailabilityUniquenessForUpdate(Long roomTypeId, LocalDate availabilityDate, Long excludeId) {
        Optional<Availability> existing = availabilityRepository
                .findByRoomTypeIdAndAvailabilityDate(roomTypeId, availabilityDate);
        if (existing.isPresent() && !existing.get().getId().equals(excludeId)) {
            throw new ConflictException(
                    "Availability already exists for this room type and date");
        }
    }
}