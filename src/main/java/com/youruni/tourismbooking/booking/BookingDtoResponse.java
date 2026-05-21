package com.youruni.tourismbooking.booking;
import java.math.BigDecimal;
import java.time.LocalDate;
public class BookingDtoResponse {
    private Long id;
    private String guestEmail;
    private String guestName;
    private String guestPhone;
    private Long roomTypeId;
    private String roomTypeName;
    private Long hotelId;
    private String hotelName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestsCount;
    private BigDecimal totalPrice;
    private BookingStatus status;
    public BookingDtoResponse() {
    }
    public BookingDtoResponse(Long id, String guestEmail, LocalDate checkInDate, LocalDate checkOutDate,
                              Integer guestsCount, BigDecimal totalPrice, BookingStatus status) {
        this.id = id;
        this.guestEmail = guestEmail;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestsCount = guestsCount;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getGuestEmail() {
        return guestEmail;
    }
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
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
    public Long getRoomTypeId() {
        return roomTypeId;
    }
    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
    public String getRoomTypeName() {
        return roomTypeName;
    }
    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }
    public Long getHotelId() {
        return hotelId;
    }
    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }
    public String getHotelName() {
        return hotelName;
    }
    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
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
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    public BookingStatus getStatus() {
        return status;
    }
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}