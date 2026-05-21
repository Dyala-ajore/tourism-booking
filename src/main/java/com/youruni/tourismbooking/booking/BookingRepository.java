package com.youruni.tourismbooking.booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    List<Booking> findByGuestEmail(String guestEmail);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByRoomType_Id(Long roomTypeId);
    List<Booking> findByGuestEmailAndStatus(String guestEmail, BookingStatus status);
}