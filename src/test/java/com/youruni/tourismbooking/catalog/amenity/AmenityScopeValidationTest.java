package com.youruni.tourismbooking.catalog.amenity;

import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.common.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AmenityScopeValidationTest {

    @Autowired private AmenityService amenityService;
    @Autowired private AmenityRepository amenityRepository;
    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;

    private Hotel testHotel;
    private RoomType testRoomType;
    private Amenity hotelScopeAmenity;
    private Amenity roomTypeScopeAmenity;
    private Amenity bothScopeAmenity;

    @BeforeEach
    void setUp() {
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setCity("Test City");
        testHotel.setCountry("Test Country");
        testHotel.setAddress("123 Test St");
        testHotel = hotelRepository.save(testHotel);

        testRoomType = new RoomType();
        testRoomType.setHotel(testHotel);
        testRoomType.setName("Deluxe Room");
        testRoomType.setCapacity(2);
        testRoomType.setBasePricePerNight(new BigDecimal("100.00"));
        testRoomType.setTotalRooms(10);
        testRoomType.setActive(true);
        testRoomType = roomTypeRepository.save(testRoomType);

        hotelScopeAmenity = createAmenity("Hotel Pool Test", AmenityScope.HOTEL, AmenityType.ENTERTAINMENT);
        roomTypeScopeAmenity = createAmenity("Room WiFi Test", AmenityScope.ROOM_TYPE, AmenityType.BASIC);
        bothScopeAmenity = createAmenity("TV Both Test", AmenityScope.BOTH, AmenityType.ENTERTAINMENT);
    }

    @Test
    void testAssignHotelAmenityToHotel_Success() {
        assertDoesNotThrow(() ->
                amenityService.assignAmenityToHotel(testHotel.getId(), hotelScopeAmenity.getId())
        );

        Hotel updatedHotel = hotelRepository.findById(testHotel.getId()).orElseThrow();
        assertTrue(updatedHotel.getAmenities().contains(hotelScopeAmenity));
    }

    @Test
    void testAssignBothAmenityToHotel_Success() {
        assertDoesNotThrow(() ->
                amenityService.assignAmenityToHotel(testHotel.getId(), bothScopeAmenity.getId())
        );

        Hotel updatedHotel = hotelRepository.findById(testHotel.getId()).orElseThrow();
        assertTrue(updatedHotel.getAmenities().contains(bothScopeAmenity));
    }

    @Test
    void testAssignRoomTypeScopeAmenityToHotel_Fails() {
        assertThrows(BadRequestException.class, () ->
                amenityService.assignAmenityToHotel(testHotel.getId(), roomTypeScopeAmenity.getId())
        );
    }

    @Test
    void testAssignRoomTypeAmenityToRoomType_Success() {
        assertDoesNotThrow(() ->
                amenityService.assignAmenityToRoomType(testRoomType.getId(), roomTypeScopeAmenity.getId())
        );

        RoomType updatedRoomType = roomTypeRepository.findById(testRoomType.getId()).orElseThrow();
        assertTrue(updatedRoomType.getAmenitySet().contains(roomTypeScopeAmenity));
    }

    @Test
    void testAssignBothAmenityToRoomType_Success() {
        assertDoesNotThrow(() ->
                amenityService.assignAmenityToRoomType(testRoomType.getId(), bothScopeAmenity.getId())
        );

        RoomType updatedRoomType = roomTypeRepository.findById(testRoomType.getId()).orElseThrow();
        assertTrue(updatedRoomType.getAmenitySet().contains(bothScopeAmenity));
    }

    @Test
    void testAssignHotelScopeAmenityToRoomType_Fails() {
        assertThrows(BadRequestException.class, () ->
                amenityService.assignAmenityToRoomType(testRoomType.getId(), hotelScopeAmenity.getId())
        );
    }

    private Amenity createAmenity(String name, AmenityScope scope, AmenityType type) {
        Amenity amenity = new Amenity();
        amenity.setName(name);
        amenity.setScope(scope);
        amenity.setType(type);
        amenity.setActive(true);
        return amenityRepository.save(amenity);
    }
}