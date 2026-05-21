package com.youruni.tourismbooking.catalog.amenity;
import org.springframework.stereotype.Component;
@Component
public class AmenityMapper {
    public Amenity toEntity(AmenityDtoRequest request) {
        if (request == null) return null;
        Amenity amenity = new Amenity();
        amenity.setName(request.getName() != null ? request.getName().trim() : null);
        amenity.setDescription(request.getDescription());
        amenity.setType(request.getType());
        amenity.setScope(request.getScope());
        amenity.setActive(request.getActive() != null ? request.getActive() : true);
        return amenity;
    }
    public AmenityDtoResponse toResponse(Amenity amenity) {
        if (amenity == null) return null;
        AmenityDtoResponse response = new AmenityDtoResponse();
        response.setId(amenity.getId());
        response.setName(amenity.getName());
        response.setDescription(amenity.getDescription());
        response.setType(amenity.getType());
        response.setScope(amenity.getScope());
        response.setActive(amenity.getActive());
        response.setCreatedAt(amenity.getCreatedAt());
        response.setUpdatedAt(amenity.getUpdatedAt());
        return response;
    }
    public void updateEntity(AmenityDtoRequest request, Amenity amenity) {
        if (request == null || amenity == null) return;
        if (request.getName() != null) {
            amenity.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            amenity.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            amenity.setType(request.getType());
        }
        if (request.getScope() != null) {
            amenity.setScope(request.getScope());
        }
        if (request.getActive() != null) {
            amenity.setActive(request.getActive());
        }
    }
}