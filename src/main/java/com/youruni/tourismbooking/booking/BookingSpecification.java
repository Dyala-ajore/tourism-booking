package com.youruni.tourismbooking.booking;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
public class BookingSpecification {
    private BookingSpecification() {
    }
    public static Specification<Booking> withFilters(String guestEmail, BookingStatus status,
                                                     Long roomTypeId, LocalDate startDate, LocalDate endDate) {
        return Specification.where(byGuestEmail(guestEmail))
                .and(byStatus(status))
                .and(byRoomTypeId(roomTypeId))
                .and(byCheckInDateRange(startDate, endDate));
    }
    public static Specification<Booking> byGuestEmail(String guestEmail) {
        if (guestEmail == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("guestEmail"), guestEmail);
    }
    public static Specification<Booking> byStatus(BookingStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), status);
    }
    public static Specification<Booking> byRoomTypeId(Long roomTypeId) {
        if (roomTypeId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("roomType").get("id"), roomTypeId);
    }
    public static Specification<Booking> byCheckInDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.and(
                            criteriaBuilder.greaterThanOrEqualTo(root.get("checkInDate"), startDate),
                            criteriaBuilder.lessThanOrEqualTo(root.get("checkOutDate"), endDate)
                    );
        }
        if (startDate != null) {
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("checkInDate"), startDate);
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("checkOutDate"), endDate);
    }
    public static Specification<Booking> byManagerHotels(Long managerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("roomType").get("hotel").get("managedByUserId"), managerId);
    }
}