package com.youruni.tourismbooking.availabilityPricing;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "availabilities", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_type_id", "availability_date"})
})
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;
    @Column(name = "availability_date", nullable = false)
    private LocalDate availabilityDate;
    @Column(nullable = false)
    private Integer availableRooms;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerNight;
    @Column(length = 500)
    private String notes;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    public Availability() {
    }
    public Availability(Long roomTypeId, LocalDate availabilityDate, Integer availableRooms, BigDecimal pricePerNight) {
        this.roomTypeId = roomTypeId;
        this.availabilityDate = availabilityDate;
        this.availableRooms = availableRooms;
        this.pricePerNight = pricePerNight;
    }
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getRoomTypeId() {
        return roomTypeId;
    }
    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
    public LocalDate getAvailabilityDate() {
        return availabilityDate;
    }
    public void setAvailabilityDate(LocalDate availabilityDate) {
        this.availabilityDate = availabilityDate;
    }
    public Integer getAvailableRooms() {
        return availableRooms;
    }
    public void setAvailableRooms(Integer availableRooms) {
        this.availableRooms = availableRooms;
    }
    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}