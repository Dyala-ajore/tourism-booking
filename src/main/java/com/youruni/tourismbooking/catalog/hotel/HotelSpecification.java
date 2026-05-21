package com.youruni.tourismbooking.catalog.hotel;
import org.springframework.data.jpa.domain.Specification;
public class HotelSpecification {
    public static Specification<Hotel> nameContains(String name) {
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
    public static Specification<Hotel> cityEquals(String city) {
        return (root, query, criteriaBuilder) -> {
            if (city == null || city.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("city")),
                    city.toLowerCase()
            );
        };
    }
    public static Specification<Hotel> countryEquals(String country) {
        return (root, query, criteriaBuilder) -> {
            if (country == null || country.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("country")),
                    country.toLowerCase()
            );
        };
    }
    public static Specification<Hotel> withFilters(String name, String city, String country) {
        return Specification.where(nameContains(name))
                .and(cityEquals(city))
                .and(countryEquals(country));
    }
}