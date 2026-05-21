package com.youruni.tourismbooking.availabilityPricing;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public class AvailabilityDtoRequest {
    @NotNull(message = "Room type ID cannot be null")
    @Positive(message = "Room type ID must be positive")
    private Long roomTypeId;
    @NotNull(message = "Availability date cannot be null")
    @FutureOrPresent(message = "Availability date must be today or in the future")
    private LocalDate availabilityDate;
    @NotNull(message = "Available rooms cannot be null")
    @Min(value = 0, message = "Available rooms cannot be negative")
    @Max(value = 1000, message = "Available rooms cannot exceed 1000")
    private Integer availableRooms;
    @NotNull(message = "Price per night cannot be null")
    @DecimalMin(value = "0.01", message = "Price per night must be greater than 0")
    private BigDecimal pricePerNight;
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
    public AvailabilityDtoRequest() {
    }
    public AvailabilityDtoRequest(Long roomTypeId, LocalDate availabilityDate, Integer availableRooms, BigDecimal pricePerNight) {
        this.roomTypeId = roomTypeId;
        this.availabilityDate = availabilityDate;
        this.availableRooms = availableRooms;
        this.pricePerNight = pricePerNight;
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
}