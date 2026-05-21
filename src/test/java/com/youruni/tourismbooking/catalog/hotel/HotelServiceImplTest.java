package com.youruni.tourismbooking.catalog.hotel;

import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.ConflictException;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // 🔥 يحل مشكلة Mockito
class HotelServiceImplTest {

    @Mock private HotelRepository hotelRepository;
    @Mock private HotelMapper hotelMapper;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private HotelDtoRequest hotelRequest;
    private Hotel hotel;

    @BeforeEach
    void setUp() {

        // 🔥 حل مشكلة current user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager", null)
        );

        User user = new User();
        user.setId(10L);
        user.setUsername("manager");

        // 🔥 lenient لتجنب UnnecessaryStubbing
        lenient().when(userRepository.findByUsername("manager"))
                .thenReturn(Optional.of(user));

        hotelRequest = new HotelDtoRequest();
        hotelRequest.setName("Grand Hotel");
        hotelRequest.setCity("Paris");
        hotelRequest.setCountry("France");
        hotelRequest.setAddress("123 Main Street");
        hotelRequest.setDescription("Luxury");

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Grand Hotel");
        hotel.setCity("Paris");
        hotel.setCountry("France");
        hotel.setAddress("123 Main Street");
        hotel.setManagedByUserId(10L);
    }

    @Test
    void createHotel_Success() {

        when(hotelRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        when(hotelMapper.toEntity(any())).thenReturn(hotel);
        when(hotelRepository.save(any())).thenReturn(hotel);
        when(hotelMapper.toResponseDto(any())).thenReturn(new HotelDtoResponse());

        HotelDtoResponse result = hotelService.createHotel(hotelRequest);

        assertNotNull(result);
    }

    @Test
    void createHotel_Duplicate() {

        when(hotelRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        // 🔥 الحل هنا: ConflictException وليس BadRequest
        assertThrows(ConflictException.class,
                () -> hotelService.createHotel(hotelRequest));
    }

    @Test
    void updateHotel_Success() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

        when(hotelRepository.existsByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        when(hotelRepository.findByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(hotel));

        when(hotelRepository.save(any())).thenReturn(hotel);
        when(hotelMapper.toResponseDto(any())).thenReturn(new HotelDtoResponse());

        HotelDtoResponse result = hotelService.updateHotel(1L, hotelRequest);

        assertNotNull(result);
    }

    @Test
    void deleteHotel_Success() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel_Id(1L)).thenReturn(new ArrayList<>());

        hotelService.deleteHotel(1L);

        verify(hotelRepository).delete(hotel);
    }

    @Test
    void deleteHotel_WithRooms() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(roomTypeRepository.findByHotel_Id(1L)).thenReturn(List.of(new RoomType()));

        assertThrows(BadRequestException.class,
                () -> hotelService.deleteHotel(1L));
    }

    @Test
    void getHotelById_Success() {

        when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toResponseDto(hotel)).thenReturn(new HotelDtoResponse());

        HotelDtoResponse result = hotelService.getHotelById(1L);

        assertNotNull(result);
    }

    @Test
    void getHotelById_NotFound() {

        when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> hotelService.getHotelById(99L));
    }
}