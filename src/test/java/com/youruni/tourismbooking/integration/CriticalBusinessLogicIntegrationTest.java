package com.youruni.tourismbooking.integration;

import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.booking.BookingRepository;
import com.youruni.tourismbooking.catalog.hotel.HotelDtoRequest;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CriticalBusinessLogicIntegrationTest extends BaseIntegrationTest {

    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private AvailabilityRepository availabilityRepository;
    @Autowired private UserRepository userRepository;

    private Authentication managerAuth;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        availabilityRepository.deleteAll();
        roomTypeRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        User manager = new User();
        manager.setUsername("manager");
        manager.setEmail("manager@test.com");
        manager.setFullName("Manager User");
        manager.setPassword("password");
        manager.setRole(UserRole.MANAGER);
        manager.setEnabled(true);
        userRepository.save(manager);

        managerAuth = new UsernamePasswordAuthenticationToken(
                "manager",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MANAGER"))
        );

        SecurityContextHolder.getContext().setAuthentication(managerAuth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private HotelDtoRequest hotelRequest(String name, String city, String country, String address, String description) {
        HotelDtoRequest request = new HotelDtoRequest();
        request.setName(name);
        request.setCity(city);
        request.setCountry(country);
        request.setAddress(address);
        request.setDescription(description);
        return request;
    }

    @Test
    void createHotel_UniqueHotel_Returns201Created() throws Exception {
        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", "A beautiful luxury hotel")
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Luxury Hotel"))
                .andExpect(jsonPath("$.city").value("Paris"));
    }

    @Test
    void createHotel_DuplicateExactMatch_Returns409Conflict() throws Exception {
        HotelDtoRequest request = hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", "A beautiful luxury hotel");

        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void createHotel_CaseInsensitiveDuplicate_Returns409Conflict() throws Exception {
        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("luxury hotel", "paris", "france", "123 rue main", null)
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void createHotel_WhitespaceCloneDuplicate_Returns409Conflict() throws Exception {
        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("  Luxury Hotel  ", "  Paris  ", "  France  ", "  123 Rue Main  ", null)
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void updateHotel_ToSameValues_Returns200Ok() throws Exception {
        String createResponse = mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", "Original description")
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long hotelId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/hotels/" + hotelId)
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", "Updated description")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hotelId));
    }

    @Test
    void updateHotel_ToConflictingValues_Returns409Conflict() throws Exception {
        mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isCreated());

        String response2 = mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Grand Hotel", "Lyon", "France", "456 Avenue", null)
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long hotel2Id = objectMapper.readTree(response2).get("id").asLong();

        mockMvc.perform(put("/api/hotels/" + hotel2Id)
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isConflict());
    }

    @Test
    void updateHotel_ToUniqueValues_Returns200Ok() throws Exception {
        String createResponse = mockMvc.perform(post("/api/hotels")
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long hotelId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(put("/api/hotels/" + hotelId)
                        .principal(managerAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                hotelRequest("Luxury Premier Hotel", "Paris", "France", "123 Rue Main", null)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Luxury Premier Hotel"));
    }
}