package com.youruni.tourismbooking.availabilityPricing;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDate;
public class AvailabilitySpecification {
    public static Specification<Availability> byRoomTypeId(Long roomTypeId) {
        return (root, query, criteriaBuilder) -> {
            if (roomTypeId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("roomTypeId"), roomTypeId);
        };
    }
    public static Specification<Availability> byAvailabilityDate(LocalDate availabilityDate) {
        return (root, query, criteriaBuilder) -> {
            if (availabilityDate == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("availabilityDate"), availabilityDate);
        };
    }
    public static Specification<Availability> byDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null;
            }
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("availabilityDate"), startDate, endDate);
            }
            if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("availabilityDate"), startDate);
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("availabilityDate"), endDate);
        };
    }
    public static Specification<Availability> byMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice);
        };
    }
    public static Specification<Availability> byMaxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice);
        };
    }
    public static Specification<Availability> onlyAvailable(Boolean onlyAvailable) {
        return (root, query, criteriaBuilder) -> {
            if (onlyAvailable == null || !onlyAvailable) {
                return null;
            }
            return criteriaBuilder.greaterThan(root.get("availableRooms"), 0);
        };
    }
    public static Specification<Availability> withFilters(
            Long roomTypeId,
            LocalDate availabilityDate,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean onlyAvailable) {
        return Specification.where(byRoomTypeId(roomTypeId))
                .and(byAvailabilityDate(availabilityDate))
                .and(byDateRange(startDate, endDate))
                .and(byMinPrice(minPrice))
                .and(byMaxPrice(maxPrice))
                .and(onlyAvailable(onlyAvailable));
    }
}