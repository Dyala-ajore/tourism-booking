package com.youruni.tourismbooking.catalog.room;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;
@Component
public class RoomTypeMapper {
    public RoomType toEntity(RoomTypeDtoRequest request) {
        if (request == null) return null;
        RoomType roomType = new RoomType();
        roomType.setName(request.getName());
        roomType.setCapacity(request.getCapacity());
        roomType.setBasePricePerNight(request.getBasePricePerNight());
        roomType.setTotalRooms(request.getTotalRooms());
        roomType.setActive(request.getActive() != null ? request.getActive() : true);
        return roomType;
    }
    public RoomTypeDtoResponse toResponse(RoomType roomType) {
        if (roomType == null) return null;
        RoomTypeDtoResponse response = new RoomTypeDtoResponse();
        response.setId(roomType.getId());
        response.setName(roomType.getName());
        response.setCapacity(roomType.getCapacity());
        response.setBasePricePerNight(roomType.getBasePricePerNight() != null 
            ? roomType.getBasePricePerNight().doubleValue() 
            : null);
        response.setTotalRooms(roomType.getTotalRooms());
        response.setActive(roomType.getActive());
        response.setImagePath(roomType.getImagePath());
        if (roomType.getAmenitySet() != null && !roomType.getAmenitySet().isEmpty()) {
            Set<String> amenityNames = roomType.getAmenitySet().stream()
                    .map(com.youruni.tourismbooking.catalog.amenity.Amenity::getName)
                    .collect(Collectors.toSet());
            response.setAmenityNames(amenityNames);
        }
        if (roomType.getHotel() != null) {
            response.setHotelId(roomType.getHotel().getId());
            response.setHotelName(roomType.getHotel().getName());
        }
        return response;
    }
    public void updateEntity(RoomTypeDtoRequest request, RoomType roomType) {
        if (request == null || roomType == null) return;
        if (request.getName() != null) roomType.setName(request.getName());
        if (request.getCapacity() != null) roomType.setCapacity(request.getCapacity());
        if (request.getBasePricePerNight() != null) roomType.setBasePricePerNight(request.getBasePricePerNight());
        if (request.getTotalRooms() != null) roomType.setTotalRooms(request.getTotalRooms());
        if (request.getActive() != null) roomType.setActive(request.getActive());
    }
}