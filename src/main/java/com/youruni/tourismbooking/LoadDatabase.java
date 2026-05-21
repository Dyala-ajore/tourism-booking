package com.youruni.tourismbooking;

import com.youruni.tourismbooking.availabilityPricing.Availability;
import com.youruni.tourismbooking.availabilityPricing.AvailabilityRepository;
import com.youruni.tourismbooking.catalog.amenity.Amenity;
import com.youruni.tourismbooking.catalog.amenity.AmenityRepository;
import com.youruni.tourismbooking.catalog.amenity.AmenityScope;
import com.youruni.tourismbooking.catalog.amenity.AmenityType;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import com.youruni.tourismbooking.catalog.hotel.HotelRepository;
import com.youruni.tourismbooking.catalog.room.RoomType;
import com.youruni.tourismbooking.catalog.room.RoomTypeRepository;
import com.youruni.tourismbooking.user.User;
import com.youruni.tourismbooking.user.UserRepository;
import com.youruni.tourismbooking.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile; // ⭐ مهم
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Profile("!test")
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository,
            AmenityRepository amenityRepository,
            AvailabilityRepository availabilityRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            log.info("===== Starting Database Seeding =====");

            seedUsers(userRepository, passwordEncoder);
            seedAmenities(amenityRepository);
            seedHotelsAndRoomTypes(hotelRepository, roomTypeRepository, amenityRepository);
            seedAvailability(roomTypeRepository, availabilityRepository);

            log.info("===== Database Seeding Complete =====");
        };
    }

    private void seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        upsertUser(userRepository, passwordEncoder, "admin", "admin@example.com", "Admin User", "admin123", UserRole.ADMIN);
        upsertUser(userRepository, passwordEncoder, "manager", "manager@example.com", "Manager User", "manager123", UserRole.MANAGER);
        upsertUser(userRepository, passwordEncoder, "guest", "guest@example.com", "Guest User", "guest123", UserRole.GUEST);
    }

    private void upsertUser(UserRepository repo, PasswordEncoder encoder,
                            String username, String email, String name, String pass, UserRole role) {

        User user = repo.findByUsername(username).orElse(null);

        if (user == null) {
            user = new User();
            user.setUsername(username);
        }

        user.setEmail(email);
        user.setFullName(name);
        user.setPassword(encoder.encode(pass));
        user.setRole(role);
        user.setEnabled(true);

        repo.save(user);
    }

    private void seedAmenities(AmenityRepository repo) {

        String[] names = {
                "WiFi", "Swimming Pool", "Fitness Center",
                "Air Conditioning", "King Bed", "Mini Bar",
                "Room Service", "Spa"
        };

        for (String name : names) {
            if (!repo.existsByNameIgnoreCase(name)) {
                Amenity a = new Amenity();
                a.setName(name);
                a.setActive(true);
                a.setScope(AmenityScope.BOTH);
                a.setType(AmenityType.COMFORT);
                repo.save(a);
            }
        }
    }

    private void seedHotelsAndRoomTypes(
            HotelRepository hotelRepo,
            RoomTypeRepository roomRepo,
            AmenityRepository amenityRepo) {

        if (hotelRepo.count() > 0) return;

        Hotel hotel = new Hotel();
        hotel.setName("Sample Hotel");
        hotel.setCity("Amman");
        hotel.setCountry("Jordan");
        hotel.setAddress("Test Address");

        hotel = hotelRepo.save(hotel);

        RoomType room = new RoomType();
        room.setHotel(hotel);
        room.setName("Standard Room");
        room.setCapacity(2);
        room.setBasePricePerNight(new BigDecimal("100"));
        room.setTotalRooms(10);
        room.setActive(true);

        roomRepo.save(room);
    }

    private void seedAvailability(
            RoomTypeRepository roomRepo,
            AvailabilityRepository availabilityRepo) {

        for (RoomType rt : roomRepo.findAll()) {

            for (int i = 0; i < 5; i++) {
                LocalDate date = LocalDate.now().plusDays(i);

                if (availabilityRepo.findByRoomTypeIdAndAvailabilityDate(rt.getId(), date).isEmpty()) {

                    Availability a = new Availability();
                    a.setRoomTypeId(rt.getId());
                    a.setAvailabilityDate(date);
                    a.setAvailableRooms(rt.getTotalRooms());
                    a.setPricePerNight(rt.getBasePricePerNight());

                    availabilityRepo.save(a);
                }
            }
        }
    }
}