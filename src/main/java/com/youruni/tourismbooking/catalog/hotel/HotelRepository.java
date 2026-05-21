package com.youruni.tourismbooking.catalog.hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
    boolean existsByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(String name, String city, String country, String address);
    java.util.Optional<Hotel> findByNameIgnoreCaseAndCityIgnoreCaseAndCountryIgnoreCaseAndAddressIgnoreCase(String name, String city, String country, String address);
}