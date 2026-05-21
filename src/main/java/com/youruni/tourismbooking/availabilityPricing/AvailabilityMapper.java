package com.youruni.tourismbooking.availabilityPricing;
import org.springframework.stereotype.Component;
@Component
public class AvailabilityMapper {
    public AvailabilityDtoResponse toResponseDto(Availability availability) {
        if (availability == null) {
            return null;
        }
        AvailabilityDtoResponse response = new AvailabilityDtoResponse();
        response.setId(availability.getId());
        response.setRoomTypeId(availability.getRoomTypeId());
        response.setAvailabilityDate(availability.getAvailabilityDate());
        response.setAvailableRooms(availability.getAvailableRooms());
        response.setPricePerNight(availability.getPricePerNight());
        response.setNotes(availability.getNotes());
        response.setCreatedAt(availability.getCreatedAt());
        response.setUpdatedAt(availability.getUpdatedAt());
        return response;
    }
    public Availability toEntity(AvailabilityDtoRequest requestDto) {
        if (requestDto == null) {
            return null;
        }
        Availability availability = new Availability();
        availability.setRoomTypeId(requestDto.getRoomTypeId());
        availability.setAvailabilityDate(requestDto.getAvailabilityDate());
        availability.setAvailableRooms(requestDto.getAvailableRooms());
        availability.setPricePerNight(requestDto.getPricePerNight());
        availability.setNotes(requestDto.getNotes());
        return availability;
    }
    public void updateEntityFromDto(AvailabilityDtoRequest requestDto, Availability availability) {
        if (requestDto == null || availability == null) {
            return;
        }
        if (requestDto.getRoomTypeId() != null) {
            availability.setRoomTypeId(requestDto.getRoomTypeId());
        }
        if (requestDto.getAvailabilityDate() != null) {
            availability.setAvailabilityDate(requestDto.getAvailabilityDate());
        }
        if (requestDto.getAvailableRooms() != null) {
            availability.setAvailableRooms(requestDto.getAvailableRooms());
        }
        if (requestDto.getPricePerNight() != null) {
            availability.setPricePerNight(requestDto.getPricePerNight());
        }
        if (requestDto.getNotes() != null) {
            availability.setNotes(requestDto.getNotes());
        }
    }
}