package com.youruni.tourismbooking.catalog.hotel;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 1000)
    private String description;

    @Column(length = 500)
    private String imagePath;

    @Column(name = "managed_by_user_id")
    private Long managedByUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<com.youruni.tourismbooking.catalog.amenity.Amenity> amenities = new LinkedHashSet<>();

    // ✅ FIX: إضافة العلاقة مع RoomType
    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    private Set<com.youruni.tourismbooking.catalog.room.RoomType> roomTypes = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getImagePath() { return imagePath; }
    public Long getManagedByUserId() { return managedByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Set<com.youruni.tourismbooking.catalog.amenity.Amenity> getAmenities() { return amenities; }
    public Set<com.youruni.tourismbooking.catalog.room.RoomType> getRoomTypes() { return roomTypes; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCity(String city) { this.city = city; }
    public void setCountry(String country) { this.country = country; }
    public void setAddress(String address) { this.address = address; }
    public void setDescription(String description) { this.description = description; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setManagedByUserId(Long managedByUserId) { this.managedByUserId = managedByUserId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setAmenities(Set<com.youruni.tourismbooking.catalog.amenity.Amenity> amenities) { this.amenities = amenities; }
    public void setRoomTypes(Set<com.youruni.tourismbooking.catalog.room.RoomType> roomTypes) { this.roomTypes = roomTypes; }
}