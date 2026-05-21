package com.youruni.tourismbooking.catalog.room;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "Request object for creating/updating a room type")
public class RoomTypeDtoRequest {
    @Schema(description = "Hotel ID", example = "1")
    @NotNull(message = "Hotel ID is required")
    private Long hotelId;
    @Schema(description = "Room type name", example = "Deluxe Suite")
    @NotBlank(message = "Room type name is required")
    @Size(min = 2, max = 100, message = "Room type name must be between 2 and 100 characters")
    private String name;
    @Schema(description = "Maximum capacity", example = "4")
    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
    @Schema(description = "Base price per night", example = "150.0")
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double basePricePerNight;
    @Schema(description = "Total number of rooms of this type", example = "10")
    @NotNull(message = "Total rooms is required")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;
    @Schema(description = "Whether the room type is active", example = "true")
    private Boolean active = true;
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Double getBasePricePerNight() { return basePricePerNight; }
    public void setBasePricePerNight(Double basePricePerNight) { this.basePricePerNight = basePricePerNight; }
    public Integer getTotalRooms() { return totalRooms; }
    public void setTotalRooms(Integer totalRooms) { this.totalRooms = totalRooms; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}