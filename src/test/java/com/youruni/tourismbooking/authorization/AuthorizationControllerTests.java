package com.youruni.tourismbooking.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthorizationControllerTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private HotelRepository hotelRepository;
    @Autowired private RoomTypeRepository roomTypeRepository;

    @BeforeEach
    void setup() {

        // USERS
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("hashed");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setFullName("Admin User");
        adminUser.setEnabled(true);
        userRepository.save(adminUser);

        User managerUser = new User();
        managerUser.setUsername("manager");
        managerUser.setEmail("manager@test.com");
        managerUser.setPassword("hashed");
        managerUser.setRole(UserRole.MANAGER);
        managerUser.setFullName("Manager User");
        managerUser.setEnabled(true);
        userRepository.save(managerUser);

        User guestUser = new User();
        guestUser.setUsername("guest");
        guestUser.setEmail("guest@test.com");
        guestUser.setPassword("hashed");
        guestUser.setRole(UserRole.GUEST);
        guestUser.setFullName("Guest User");
        guestUser.setEnabled(true);
        userRepository.save(guestUser);

        // HOTEL
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");
        hotel.setCountry("Test Country");
        hotel.setAddress("Test Address");
        hotel = hotelRepository.save(hotel);

        // ROOM TYPE (⭐ مهم)
        RoomType room = new RoomType();
        room.setName("Standard Room");
        room.setCapacity(2);
        room.setTotalRooms(10);
        room.setBasePricePerNight(new BigDecimal("100")); // ⭐ لا تحذفيها
        room.setHotel(hotel);
        room.setActive(true);
        roomTypeRepository.save(room);
    }

    @Test
    @WithMockUser(username = "guest", roles = "GUEST")
    void testGuestCanViewHotels() throws Exception {
        mockMvc.perform(get("/api/hotels"))
                .andExpect(status().isOk());
    }
}