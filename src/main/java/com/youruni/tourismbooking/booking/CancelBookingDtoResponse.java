package com.youruni.tourismbooking.booking;
import java.time.LocalDate;
public class CancelBookingDtoResponse {
    private Long bookingId;
    private BookingStatus newStatus;
    private String guestEmail;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String message;
    public CancelBookingDtoResponse() {
    }
    public CancelBookingDtoResponse(Long bookingId, BookingStatus newStatus, String guestEmail) {
        this.bookingId = bookingId;
        this.newStatus = newStatus;
        this.guestEmail = guestEmail;
        this.message = "Booking cancelled successfully";
    }
    public Long getBookingId() {
        return bookingId;
    }
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    public BookingStatus getNewStatus() {
        return newStatus;
    }
    public void setNewStatus(BookingStatus newStatus) {
        this.newStatus = newStatus;
    }
    public String getGuestEmail() {
        return guestEmail;
    }
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
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
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}