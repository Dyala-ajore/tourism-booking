package com.youruni.tourismbooking.catalog.amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;
public interface AmenityRepository extends JpaRepository<Amenity, Long>, JpaSpecificationExecutor<Amenity> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Amenity> findByNameIgnoreCase(String name);
    List<Amenity> findByActiveTrue();
    List<Amenity> findByScope(AmenityScope scope);
    List<Amenity> findByType(AmenityType type);
    List<Amenity> findByNameContainingIgnoreCase(String nameFilter);
    List<Amenity> findByScopeAndActiveTrue(AmenityScope scope);
    List<Amenity> findByTypeAndActiveTrue(AmenityType type);
}