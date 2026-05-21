package com.youruni.tourismbooking.availabilityPricing;
import com.youruni.tourismbooking.common.PagedResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
public interface AvailabilityService {
    AvailabilityDtoResponse createAvailability(AvailabilityDtoRequest request, String authenticatedUsername, String userRole);
    AvailabilityDtoResponse getAvailabilityById(Long id);
    PagedResponse<AvailabilityDtoResponse> getAllAvailability(
            Long roomTypeId,
            LocalDate availabilityDate,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onlyAvailable,
            Pageable pageable
    );
    AvailabilityDtoResponse updateAvailability(Long id, AvailabilityDtoRequest request, String authenticatedUsername, String userRole);
    void deleteAvailability(Long id, String authenticatedUsername, String userRole);
    AvailabilityCheckResponse checkAvailability(
            Long roomTypeId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guests
    );
}