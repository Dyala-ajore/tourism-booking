package com.youruni.tourismbooking.booking;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class CreateBookingDtoRequest {

    @NotNull(message = "Room type ID cannot be null")
    @Positive(message = "Room type ID must be positive")
    private Long roomTypeId;

    @NotNull(message = "Check-in date cannot be null")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date cannot be null")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Guest count cannot be null")
    @Min(value = 1, message = "Guest count must be at least 1")
    @Max(value = 100, message = "Guest count cannot exceed 100")
    private Integer guestsCount;

    @NotBlank(message = "Guest name cannot be blank")
    private String guestName;

    @NotBlank(message = "Guest phone cannot be blank")
    private String guestPhone;

    // ================= GETTERS & SETTERS =================

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public Integer getGuestsCount() {
        return guestsCount;
    }

    public void setGuestsCount(Integer guestsCount) {
        this.guestsCount = guestsCount;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public void setGuestPhone(String guestPhone) {
        this.guestPhone = guestPhone;
    }

    // ================= VALIDATION =================

    @AssertTrue(message = "Check-out date must be after check-in date")
    @JsonIgnore
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) return true;
        return checkOutDate.isAfter(checkInDate);
    }
}