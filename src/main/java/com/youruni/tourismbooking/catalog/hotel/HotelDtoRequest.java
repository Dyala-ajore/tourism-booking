package com.youruni.tourismbooking.catalog.hotel;
import jakarta.validation.constraints.*;
public class HotelDtoRequest {
    @NotBlank(message = "Hotel name cannot be blank")
    @Size(min = 2, max = 100, message = "Hotel name must be between 2 and 100 characters")
    private String name;
    @NotBlank(message = "City cannot be blank")
    @Size(min = 2, max = 100, message = "City must be between 2 and 100 characters")
    private String city;
    @NotBlank(message = "Country cannot be blank")
    @Size(min = 2, max = 100, message = "Country must be between 2 and 100 characters")
    private String country;
    @NotBlank(message = "Address cannot be blank")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    private Long managedByUserId;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getManagedByUserId() { return managedByUserId; }
    public void setManagedByUserId(Long managedByUserId) { this.managedByUserId = managedByUserId; }
}