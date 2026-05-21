package com.youruni.tourismbooking.catalog.hotel;

import org.springframework.stereotype.Component;
import com.youruni.tourismbooking.catalog.room.RoomTypeMapper;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class HotelMapper {

    private final RoomTypeMapper roomTypeMapper;

    public HotelMapper(RoomTypeMapper roomTypeMapper) {
        this.roomTypeMapper = roomTypeMapper;
    }

    public HotelDtoResponse toResponseDto(Hotel hotel) {
        if (hotel == null) return null;

        HotelDtoResponse response = new HotelDtoResponse();

        response.setId(hotel.getId());
        response.setName(hotel.getName());
        response.setCity(hotel.getCity());
        response.setCountry(hotel.getCountry());
        response.setAddress(hotel.getAddress());
        response.setDescription(hotel.getDescription());
        response.setImagePath(hotel.getImagePath());
        response.setCreatedAt(hotel.getCreatedAt());
        response.setUpdatedAt(hotel.getUpdatedAt());

        // ✅ FIX: amenities never null
        if (hotel.getAmenities() != null && !hotel.getAmenities().isEmpty()) {
            response.setAmenityNames(
                    hotel.getAmenities().stream()
                            .map(a -> a.getName())
                            .collect(Collectors.toSet())
            );
        } else {
            response.setAmenityNames(Set.of());
        }

        // ✅ FIX: roomTypes never null
        if (hotel.getRoomTypes() != null && !hotel.getRoomTypes().isEmpty()) {
            response.setRoomTypes(
                    hotel.getRoomTypes().stream()
                            .map(roomTypeMapper::toResponse)
                            .collect(Collectors.toList())
            );
        } else {
            response.setRoomTypes(List.of());
        }

        return response;
    }

    public Hotel toEntity(HotelDtoRequest requestDto) {
        if (requestDto == null) return null;

        Hotel hotel = new Hotel();
        hotel.setName(requestDto.getName());
        hotel.setCity(requestDto.getCity());
        hotel.setCountry(requestDto.getCountry());
        hotel.setAddress(requestDto.getAddress());
        hotel.setDescription(requestDto.getDescription());

        return hotel;
    }

    public void updateEntityFromDto(HotelDtoRequest requestDto, Hotel hotel) {
        if (requestDto == null || hotel == null) return;

        if (requestDto.getName() != null) hotel.setName(requestDto.getName());
        if (requestDto.getCity() != null) hotel.setCity(requestDto.getCity());
        if (requestDto.getCountry() != null) hotel.setCountry(requestDto.getCountry());
        if (requestDto.getAddress() != null) hotel.setAddress(requestDto.getAddress());
        if (requestDto.getDescription() != null) hotel.setDescription(requestDto.getDescription());
    }
}