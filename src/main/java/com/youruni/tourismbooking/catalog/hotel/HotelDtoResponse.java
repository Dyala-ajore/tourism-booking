package com.youruni.tourismbooking.catalog.hotel;

import io.swagger.v3.oas.annotations.media.Schema;
import com.youruni.tourismbooking.catalog.room.RoomTypeDtoResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Schema(description = "Response object for hotel data")
public class HotelDtoResponse {

    private Long id;
    private String name;
    private String city;
    private String country;
    private String address;
    private String description;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ✅ FIX: default values
    private Set<String> amenityNames = new HashSet<>();
    private List<RoomTypeDtoResponse> roomTypes = new ArrayList<>();

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<String> getAmenityNames() { return amenityNames; }
    public void setAmenityNames(Set<String> amenityNames) { this.amenityNames = amenityNames; }

    public List<RoomTypeDtoResponse> getRoomTypes() { return roomTypes; }
    public void setRoomTypes(List<RoomTypeDtoResponse> roomTypes) { this.roomTypes = roomTypes; }
}