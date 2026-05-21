package com.youruni.tourismbooking.catalog.room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
public interface RoomTypeRepository extends JpaRepository<RoomType, Long>,
        JpaSpecificationExecutor<RoomType> {
    @Query("SELECT r FROM RoomType r WHERE " +
            "(:hotelId IS NULL OR r.hotel.id = :hotelId) AND " +
            "(:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:minCapacity IS NULL OR r.capacity >= :minCapacity) AND " +
            "(:maxPrice IS NULL OR r.basePricePerNight <= :maxPrice) AND " +
            "(:active IS NULL OR r.active = :active)")
    Page<RoomType> findWithFilters(
            @Param("hotelId") Long hotelId,
            @Param("name") String name,
            @Param("minCapacity") Integer minCapacity,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("active") Boolean active,
            Pageable pageable);
    List<RoomType> findByHotel_Id(Long hotelId);
    boolean existsByHotel_IdAndNameIgnoreCase(Long hotelId, String name);
    Optional<RoomType> findByHotel_IdAndNameIgnoreCase(Long hotelId, String name);
}