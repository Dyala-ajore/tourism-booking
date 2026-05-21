package com.youruni.tourismbooking.catalog.room;
import com.youruni.tourismbooking.catalog.hotel.Hotel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
@Entity
@Table(name = "room_types")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Integer capacity;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal basePricePerNight;
    @Column(nullable = false)
    private Integer totalRooms;
    @Column(nullable = false)
    private Boolean active = true;
    @Column(length = 500)
    private String imagePath;
    @ManyToMany(fetch = FetchType.LAZY, cascade = {})
    @JoinTable(
            name = "room_type_amenities",
            joinColumns = @JoinColumn(name = "room_type_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<com.youruni.tourismbooking.catalog.amenity.Amenity> amenitySet = new LinkedHashSet<>();
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Hotel getHotel() {
        return hotel;
    }
    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getCapacity() {
        return capacity;
    }
    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
    public BigDecimal getBasePricePerNight() {
        return basePricePerNight;
    }
    public void setBasePricePerNight(BigDecimal basePricePerNight) {
        this.basePricePerNight = basePricePerNight;
    }
    public void setBasePricePerNight(Double basePricePerNight) {
        this.basePricePerNight = basePricePerNight != null ? new BigDecimal(basePricePerNight) : null;
    }
    public Integer getTotalRooms() {
        return totalRooms;
    }
    public void setTotalRooms(Integer totalRooms) {
        this.totalRooms = totalRooms;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public String getImagePath() {
        return imagePath;
    }
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    public Set<com.youruni.tourismbooking.catalog.amenity.Amenity> getAmenitySet() {
        return amenitySet;
    }
    public void setAmenitySet(Set<com.youruni.tourismbooking.catalog.amenity.Amenity> amenitySet) {
        this.amenitySet = amenitySet;
    }
}