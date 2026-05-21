package com.youruni.tourismbooking.payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBooking_Id(Long bookingId);
    List<Payment> findByStatus(PaymentStatus status);
    @Query("SELECT p FROM Payment p WHERE p.booking.id = :bookingId AND p.status = 'SUCCESS'")
    Optional<Payment> findSuccessfulPaymentByBookingId(@Param("bookingId") Long bookingId);
    @Query("SELECT p FROM Payment p WHERE p.status = 'FAILED' ORDER BY p.createdAt ASC")
    List<Payment> findFailedPayments();
    Optional<Payment> findByTransactionReference(String transactionReference);
    @Query("""
SELECT p FROM Payment p
WHERE p.createdAt BETWEEN :startDate AND :endDate
ORDER BY p.createdAt DESC
""")
    List<Payment> findPaymentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("""
SELECT p FROM Payment p
WHERE p.status = :status
AND p.createdAt BETWEEN :startDate AND :endDate
ORDER BY p.createdAt DESC
""")
    List<Payment> findPaymentsByStatusAndDateRange(
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    @Query("""
SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
WHERE p.status = 'SUCCESS'
AND p.createdAt BETWEEN :startDate AND :endDate
""")
    BigDecimal calculateTotalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    long countByBooking_Id(Long bookingId);
    @Query("""
SELECT p FROM Payment p
WHERE p.booking.id = :bookingId
ORDER BY p.createdAt DESC
""")
    List<Payment> findRecentPaymentsByBookingId(
            @Param("bookingId") Long bookingId
    );
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.booking.id = :bookingId AND p.status = 'SUCCESS'")
    boolean existsSuccessfulPaymentForBooking(@Param("bookingId") Long bookingId);
}