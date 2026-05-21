package com.youruni.tourismbooking.booking;
import org.springframework.stereotype.Component;
@Component
public class BookingMapper {
    public BookingDtoResponse toResponseDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDtoResponse response = new BookingDtoResponse();
        response.setId(booking.getId());
        response.setGuestEmail(booking.getGuestEmail());
        response.setGuestName(booking.getGuestName());
        response.setGuestPhone(booking.getGuestPhone());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setGuestsCount(booking.getGuestsCount());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        if (booking.getRoomType() != null) {
            response.setRoomTypeId(booking.getRoomType().getId());
            response.setRoomTypeName(booking.getRoomType().getName());
            if (booking.getRoomType().getHotel() != null) {
                response.setHotelId(booking.getRoomType().getHotel().getId());
                response.setHotelName(booking.getRoomType().getHotel().getName());
            }
        }
        return response;
    }
    public Booking toEntity(CreateBookingDtoRequest requestDto) {
        if (requestDto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setCheckInDate(requestDto.getCheckInDate());
        booking.setCheckOutDate(requestDto.getCheckOutDate());
        booking.setGuestsCount(requestDto.getGuestsCount());
        booking.setStatus(BookingStatus.PENDING);
        return booking;
    }
    public void updateEntityFromDto(CreateBookingDtoRequest requestDto, Booking booking) {
        if (requestDto == null || booking == null) {
            return;
        }
        if (requestDto.getCheckInDate() != null) {
            booking.setCheckInDate(requestDto.getCheckInDate());
        }
        if (requestDto.getCheckOutDate() != null) {
            booking.setCheckOutDate(requestDto.getCheckOutDate());
        }
        if (requestDto.getGuestsCount() != null) {
            booking.setGuestsCount(requestDto.getGuestsCount());
        }
    }
}