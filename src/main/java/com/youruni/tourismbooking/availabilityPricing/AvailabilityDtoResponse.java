package com.youruni.tourismbooking.availabilityPricing;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
public class AvailabilityDtoResponse {
    private Long id;
    private Long roomTypeId;
    private LocalDate availabilityDate;
    private Integer availableRooms;
    private BigDecimal pricePerNight;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public AvailabilityDtoResponse() {
    }
    public AvailabilityDtoResponse(Long id, Long roomTypeId, LocalDate availabilityDate,
                                   Integer availableRooms, BigDecimal pricePerNight) {
        this.id = id;
        this.roomTypeId = roomTypeId;
        this.availabilityDate = availabilityDate;
        this.availableRooms = availableRooms;
        this.pricePerNight = pricePerNight;
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