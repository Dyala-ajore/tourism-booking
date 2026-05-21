package com.youruni.tourismbooking.catalog.hotel;
import com.youruni.tourismbooking.common.PagedResponse;
import com.youruni.tourismbooking.common.FileStorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
public interface HotelService {
    HotelDtoResponse createHotel(HotelDtoRequest request);
    HotelDtoResponse createHotel(HotelDtoRequest request, String authenticatedUsername, String userRole);
    HotelDtoResponse getHotelById(Long id);
    PagedResponse<HotelDtoResponse> getAllHotels(String name, String city, String country, Pageable pageable);
    HotelDtoResponse updateHotel(Long id, HotelDtoRequest request);
    HotelDtoResponse partialUpdateHotel(Long id, HotelDtoRequest request);
    void deleteHotel(Long id);
    HotelDtoResponse uploadHotelImage(Long id, MultipartFile file, FileStorageService fileStorageService);
}