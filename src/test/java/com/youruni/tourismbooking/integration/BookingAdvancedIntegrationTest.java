package com.youruni.tourismbooking.integration;

import com.youruni.tourismbooking.availabilityPricing.Availability;
import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.booking.BookingRepository;
import com.youruni.tourismbooking.booking.CreateBookingDtoRequest;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.payment.PaymentDtoRequest;
import com.youruni.tourismbooking.payment.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingAdvancedIntegrationTest extends BaseIntegrationTest {

    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;

    private RoomType roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeEach
    void setUp() throws Exception {
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        obtainUserToken();

        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setCity("Ramallah");
        hotel.setCountry("Palestine");
        hotel.setAddress("Test Street 1");
        hotelRepository.save(hotel);

        RoomType rt = new RoomType();
        rt.setHotel(hotel);
        rt.setName("Standard Room");
        rt.setCapacity(2);
        rt.setBasePricePerNight(100.00);
        rt.setTotalRooms(5);
        rt.setActive(true);
        roomType = roomTypeRepository.save(rt);

        checkIn = LocalDate.now().plusDays(10);
        checkOut = LocalDate.now().plusDays(12);

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            Availability av = new Availability();
            av.setRoomTypeId(roomType.getId());
            av.setAvailabilityDate(date);
            av.setAvailableRooms(3);
            av.setPricePerNight(new BigDecimal("100.00"));
            availabilityRepository.save(av);
        }
    }

    // ⭐ أهم تعديل هنا (guestName + guestPhone)
    private CreateBookingDtoRequest createBookingRequest(int guests, LocalDate in, LocalDate out) {
        CreateBookingDtoRequest req = new CreateBookingDtoRequest();
        req.setRoomTypeId(roomType.getId());
        req.setCheckInDate(in);
        req.setCheckOutDate(out);
        req.setGuestsCount(guests);
        req.setGuestName("Test User");
        req.setGuestPhone("0599999999");
        return req;
    }

    @Test
    void bookingCreation_CapacityExceeded_Fails() throws Exception {

        CreateBookingDtoRequest bookingRequest = createBookingRequest(10, checkIn, checkOut);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getBearerTokenHeader())
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void availabilityCheck_GuestsExceedCapacity_ReturnsUnavailable() throws Exception {

        LocalDate testDate = LocalDate.now().plusDays(15);

        mockMvc.perform(get("/api/availability/check")
                        .header("Authorization", getBearerTokenHeader())
                        .param("roomTypeId", String.valueOf(roomType.getId()))
                        .param("checkIn", testDate.toString())
                        .param("checkOut", testDate.plusDays(1).toString())
                        .param("guests", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }
}