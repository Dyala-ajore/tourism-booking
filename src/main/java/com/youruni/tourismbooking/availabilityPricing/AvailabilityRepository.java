package com.youruni.tourismbooking.availabilityPricing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface AvailabilityRepository extends JpaRepository<Availability, Long>, JpaSpecificationExecutor<Availability> {
    Optional<Availability> findByRoomTypeIdAndAvailabilityDate(Long roomTypeId, LocalDate date);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Availability a
        WHERE a.roomTypeId = :roomTypeId
        AND a.availabilityDate = :date
    """)
    Optional<Availability> findByRoomTypeIdAndAvailabilityDateForUpdate(
            @Param("roomTypeId") Long roomTypeId,
            @Param("date") LocalDate date
    );
    @Query("""
        SELECT a FROM Availability a
        WHERE a.roomTypeId = :roomTypeId
        AND a.availabilityDate BETWEEN :startDate AND :endDate
        ORDER BY a.availabilityDate ASC
    """)
    List<Availability> findByRoomTypeIdAndDateRange(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("""
        SELECT a FROM Availability a
        WHERE a.availabilityDate = :date
        AND a.availableRooms > 0
        ORDER BY a.pricePerNight ASC
    """)
    List<Availability> findAvailableRoomsForDate(@Param("date") LocalDate date);
    @Query("""
        SELECT a FROM Availability a
        WHERE a.roomTypeId = :roomTypeId
        AND a.availabilityDate >= CURRENT_DATE
        ORDER BY a.availabilityDate ASC
    """)
    List<Availability> findFutureAvailability(@Param("roomTypeId") Long roomTypeId);
    long countByRoomTypeId(Long roomTypeId);
}