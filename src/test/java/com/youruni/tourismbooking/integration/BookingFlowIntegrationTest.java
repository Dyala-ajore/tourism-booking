package com.youruni.tourismbooking.integration;

import com.youruni.tourismbooking.availabilityPricing.Availability;
import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.availabilityPricing.PricingService;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private PricingService pricingService;

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
        rt.setBasePricePerNight(new BigDecimal("100.00"));
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

    private CreateBookingDtoRequest createRequest(int guests, LocalDate in, LocalDate out) {
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
    void fullBookingFlow_CreateAndPay_Success() throws Exception {

        BigDecimal basePrice = BigDecimal.valueOf(100.00);
        BigDecimal expectedTotalPrice = BigDecimal.ZERO;
        LocalDate currentDate = checkIn;

        while (currentDate.isBefore(checkOut)) {
            BigDecimal nightPrice = pricingService.applyDayOfWeekMultiplier(basePrice, currentDate);
            expectedTotalPrice = expectedTotalPrice.add(nightPrice);
            currentDate = currentDate.plusDays(1);
        }

        CreateBookingDtoRequest bookingRequest = createRequest(2, checkIn, checkOut);

        MvcResult bookingResult = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getBearerTokenHeader())
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(expectedTotalPrice.doubleValue()))
                .andExpect(jsonPath("$.guestEmail").value(testEmail))
                .andReturn();

        Long bookingId = objectMapper.readTree(
                bookingResult.getResponse().getContentAsString()
        ).get("id").asLong();

        PaymentDtoRequest paymentRequest = new PaymentDtoRequest();
        paymentRequest.setBookingId(bookingId);
        paymentRequest.setAmount(expectedTotalPrice);
        paymentRequest.setPaymentMethod("CREDIT_CARD");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getBearerTokenHeader())
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bookings/" + bookingId)
                        .header("Authorization", getBearerTokenHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookingId));
    }

    @Test
    void cancelBooking_RestoresAvailability() throws Exception {

        CreateBookingDtoRequest bookingRequest = createRequest(1, checkIn, checkOut);

        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getBearerTokenHeader())
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long bookingId = objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("id").asLong();

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", getBearerTokenHeader()))
                .andExpect(status().isOk());

        availabilityRepository.findByRoomTypeIdAndDateRange(
                roomType.getId(), checkIn, checkOut.minusDays(1)
        ).forEach(av -> assertEquals(3, av.getAvailableRooms()));
    }

    @Test
    void createBooking_PastDate_ReturnsBadRequest() throws Exception {

        CreateBookingDtoRequest bookingRequest = createRequest(
                1,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", getBearerTokenHeader())
                        .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isBadRequest());
    }
}