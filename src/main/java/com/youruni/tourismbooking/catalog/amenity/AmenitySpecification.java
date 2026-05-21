package com.youruni.tourismbooking.catalog.amenity;
import org.springframework.data.jpa.domain.Specification;
public class AmenitySpecification {
    public static Specification<Amenity> nameContains(String name) {
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
    public static Specification<Amenity> byType(AmenityType type) {
        return (root, query, criteriaBuilder) -> {
            if (type == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("type"), type);
        };
    }
    public static Specification<Amenity> byScope(AmenityScope scope) {
        return (root, query, criteriaBuilder) -> {
            if (scope == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("scope"), scope);
        };
    }
    public static Specification<Amenity> isActive(Boolean active) {
        return (root, query, criteriaBuilder) -> {
            if (active == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("active"), active);
        };
    }
    public static Specification<Amenity> withFilters(
            String name,
            AmenityType type,
            AmenityScope scope,
            Boolean active) {
        return Specification.where(nameContains(name))
                .and(byType(type))
                .and(byScope(scope))
                .and(isActive(active));
    }
}