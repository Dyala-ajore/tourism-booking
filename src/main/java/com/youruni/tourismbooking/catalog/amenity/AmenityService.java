package com.youruni.tourismbooking.catalog.amenity;
import com.youruni.tourismbooking.common.PagedResponse;
import org.springframework.data.domain.Pageable;
public interface AmenityService {
    AmenityDtoResponse createAmenity(AmenityDtoRequest request);
    AmenityDtoResponse getAmenityById(Long id);
    PagedResponse<AmenityDtoResponse> getAllAmenities(String name, AmenityType type, AmenityScope scope, Boolean active, Pageable pageable);
    AmenityDtoResponse updateAmenity(Long id, AmenityDtoRequest request);
    void deleteAmenity(Long id);
    void assignAmenityToHotel(Long hotelId, Long amenityId);
    void assignAmenityToHotel(Long hotelId, Long amenityId, String authenticatedUsername);
    void removeAmenityFromHotel(Long hotelId, Long amenityId);
    void removeAmenityFromHotel(Long hotelId, Long amenityId, String authenticatedUsername);
    void assignAmenityToRoomType(Long roomTypeId, Long amenityId);
    void assignAmenityToRoomType(Long roomTypeId, Long amenityId, String authenticatedUsername);
    void removeAmenityFromRoomType(Long roomTypeId, Long amenityId);
    void removeAmenityFromRoomType(Long roomTypeId, Long amenityId, String authenticatedUsername);
}