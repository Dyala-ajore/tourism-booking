package com.youruni.tourismbooking.catalog.room;

import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.common.NotFoundException;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomTypeServiceTest {

    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private HotelRepository hotelRepository;
    @Mock private RoomTypeMapper roomTypeMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private RoomTypeService roomTypeService;

    private Hotel hotel;
    private RoomType roomType;
    private RoomTypeDtoRequest request;

    @BeforeEach
    void setUp() {

        // 🔥 حل مشكلة current user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager", null)
        );

        // 🔥 mock user
        User user = new User();
        user.setId(10L);
        user.setUsername("manager");

        lenient().when(userRepository.findByUsername("manager"))
                .thenReturn(Optional.of(user));

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setManagedByUserId(10L);

        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setHotel(hotel);
        roomType.setName("Deluxe");
        roomType.setCapacity(2);
        roomType.setTotalRooms(10);
        roomType.setBasePricePerNight(new BigDecimal("100.00"));
        roomType.setActive(true);

        request = new RoomTypeDtoRequest();
        request.setHotelId(1L);
        request.setName("Deluxe");
        request.setCapacity(2);
        request.setTotalRooms(10);
        request.setBasePricePerNight(100.0);
    }

    @Test
    void createRoomType_Success() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe"))
                .thenReturn(false);
        when(roomTypeMapper.toEntity(any())).thenReturn(roomType);
        when(roomTypeRepository.save(any())).thenReturn(roomType);

        RoomType result = roomTypeService.createRoomType(request);

        assertNotNull(result);
    }

    @Test
    void createRoomType_HotelNotFound() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> roomTypeService.createRoomType(request));
    }

    @Test
    void getRoomTypeById_Success() {

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        RoomType result = roomTypeService.getRoomTypeById(1L);

        assertNotNull(result);
    }

    @Test
    void getRoomTypeById_NotFound() {

        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> roomTypeService.getRoomTypeById(99L));
    }

    @Test
    void updateRoomType_Success() {

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.existsByHotel_IdAndNameIgnoreCase(1L, "Deluxe"))
                .thenReturn(false);
        when(roomTypeRepository.save(any())).thenReturn(roomType);

        RoomType result = roomTypeService.updateRoomType(1L, request);

        assertNotNull(result);
    }

    @Test
    void updateRoomType_NotFound() {

        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> roomTypeService.updateRoomType(99L, request));
    }

    @Test
    void deleteRoomType_Success() {

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        assertDoesNotThrow(() -> roomTypeService.deleteRoomType(1L));
    }

    @Test
    void deleteRoomType_NotFound() {

        when(roomTypeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> roomTypeService.deleteRoomType(99L));
    }
}