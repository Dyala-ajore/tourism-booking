package com.youruni.tourismbooking.catalog.room;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
public class RoomTypeSpecification {
    public static Specification<RoomType> byHotelId(Long hotelId) {
        return (root, query, criteriaBuilder) -> {
            if (hotelId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("hotel").get("id"), hotelId);
        };
    }
    public static Specification<RoomType> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"
            );
        };
    }
    public static Specification<RoomType> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
    public static Specification<RoomType> minCapacity(Integer minCapacity) {
        return (root, query, criteriaBuilder) -> {
            if (minCapacity == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("capacity"), minCapacity);
        };
    }
    public static Specification<RoomType> maxCapacity(Integer maxCapacity) {
        return (root, query, criteriaBuilder) -> {
            if (maxCapacity == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("capacity"), maxCapacity);
        };
    }
    public static Specification<RoomType> minPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("basePricePerNight"), minPrice);
        };
    }
    public static Specification<RoomType> maxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("basePricePerNight"), maxPrice);
        };
    }
    public static Specification<RoomType> withFilters(
            Long hotelId,
            String name,
            Boolean active,
            Integer minCapacity,
            Integer maxCapacity,
            BigDecimal minPrice,
            BigDecimal maxPrice) {
        return Specification.where(byHotelId(hotelId))
                .and(nameContains(name))
                .and(isActive(active))
                .and(minCapacity(minCapacity))
                .and(maxCapacity(maxCapacity))
                .and(minPrice(minPrice))
                .and(maxPrice(maxPrice));
    }
}