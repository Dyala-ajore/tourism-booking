package com.youruni.tourismbooking.availabilityPricing;

import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import com.youruni.tourismbooking.common.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private AvailabilityRepository availabilityRepository;

    @Mock
    private AvailabilityMapper availabilityMapper;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private AvailabilityServiceImpl availabilityService;

    private RoomType roomType;
    private Availability availability;

    @BeforeEach
    void setUp() {
        roomType = new RoomType();
        roomType.setId(1L);
        roomType.setName("Deluxe Room");
        roomType.setCapacity(2);
        roomType.setTotalRooms(5);
        roomType.setBasePricePerNight(new BigDecimal("100.00"));
        roomType.setActive(true);

        availability = new Availability();
        availability.setId(1L);
        availability.setRoomTypeId(1L);
        availability.setAvailableRooms(3);
        availability.setPricePerNight(new BigDecimal("100.00"));
        availability.setAvailabilityDate(LocalDate.now().plusDays(1));
    }

    @Test
    void checkAvailability_SingleNightAvailable_ReturnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, checkIn))
                .thenReturn(Optional.of(availability));
        when(pricingService.applyDayOfWeekMultiplier(new BigDecimal("100.00"), checkIn))
                .thenReturn(new BigDecimal("100.00"));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 2);

        assertTrue(result.isAvailable());
        assertEquals(new BigDecimal("100.00"), result.getTotalPrice());
        assertEquals(1, result.getNights());
    }

    @Test
    void checkAvailability_MultipleNightsAllAvailable_ReturnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate secondNight = checkIn.plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Availability av1 = createAvailability(checkIn, 3, "100.00");
        Availability av2 = createAvailability(secondNight, 3, "100.00");

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, checkIn))
                .thenReturn(Optional.of(av1));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, secondNight))
                .thenReturn(Optional.of(av2));
        when(pricingService.applyDayOfWeekMultiplier(new BigDecimal("100.00"), checkIn))
                .thenReturn(new BigDecimal("100.00"));
        when(pricingService.applyDayOfWeekMultiplier(new BigDecimal("100.00"), secondNight))
                .thenReturn(new BigDecimal("125.00"));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 2);

        assertTrue(result.isAvailable());
        assertEquals(new BigDecimal("225.00"), result.getTotalPrice());
        assertEquals(2, result.getNights());
    }

    @Test
    void checkAvailability_MissingMiddleDate_ReturnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate secondNight = checkIn.plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(3);

        Availability av1 = createAvailability(checkIn, 3, "100.00");

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, checkIn))
                .thenReturn(Optional.of(av1));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, secondNight))
                .thenReturn(Optional.empty());
        when(pricingService.applyDayOfWeekMultiplier(new BigDecimal("100.00"), checkIn))
                .thenReturn(new BigDecimal("100.00"));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 2);

        assertFalse(result.isAvailable());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertEquals(0, result.getNights());
    }

    @Test
    void checkAvailability_NoRoomsAvailable_ReturnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        availability.setAvailableRooms(0);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, checkIn))
                .thenReturn(Optional.of(availability));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 2);

        assertFalse(result.isAvailable());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
    }

    @Test
    void checkAvailability_RoomTypeNotFound_ThrowsNotFoundException() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        when(roomTypeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> availabilityService.checkAvailability(999L, checkIn, checkOut, 2));
    }

    @Test
    void checkAvailability_InvalidDateRange_ThrowsBadRequestException() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        assertThrows(BadRequestException.class,
                () -> availabilityService.checkAvailability(1L, checkIn, checkOut, 2));
    }

    @Test
    void checkAvailability_SameDateCheckInCheckOut_ThrowsBadRequestException() {
        LocalDate date = LocalDate.now().plusDays(1);

        assertThrows(BadRequestException.class,
                () -> availabilityService.checkAvailability(1L, date, date, 2));
    }

    @Test
    void checkAvailability_GuestsExceedCapacity_ReturnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 3);

        assertFalse(result.isAvailable());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertEquals(0, result.getNights());
    }

    @Test
    void checkAvailability_GuestsEqualCapacity_ReturnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = LocalDate.now().plusDays(2);

        Availability av = createAvailability(checkIn, 5, "100.00");

        when(roomTypeRepository.findById(1L)).thenReturn(Optional.of(roomType));
        when(availabilityRepository.findByRoomTypeIdAndAvailabilityDate(1L, checkIn))
                .thenReturn(Optional.of(av));
        when(pricingService.applyDayOfWeekMultiplier(new BigDecimal("100.00"), checkIn))
                .thenReturn(new BigDecimal("100.00"));

        AvailabilityCheckResponse result = availabilityService.checkAvailability(1L, checkIn, checkOut, 2);

        assertTrue(result.isAvailable());
        assertEquals(new BigDecimal("100.00"), result.getTotalPrice());
        assertEquals(1, result.getNights());
    }

    private Availability createAvailability(LocalDate date, int rooms, String price) {
        Availability av = new Availability();
        av.setRoomTypeId(1L);
        av.setAvailableRooms(rooms);
        av.setPricePerNight(new BigDecimal(price));
        av.setAvailabilityDate(date);
        return av;
    }
}