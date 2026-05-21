package com.youruni.tourismbooking.catalog.room;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
@Schema(description = "Response object for room type data")
public class RoomTypeDtoResponse {
    @Schema(description = "Room type ID", example = "1")
    private Long id;
    @Schema(description = "Hotel ID")
    private Long hotelId;
    @Schema(description = "Hotel name")
    private String hotelName;
    @Schema(description = "Room type name", example = "Deluxe Suite")
    private String name;
    @Schema(description = "Maximum capacity", example = "4")
    private Integer capacity;
    @Schema(description = "Base price per night", example = "150.0")
    private Double basePricePerNight;
    @Schema(description = "Total number of rooms", example = "10")
    private Integer totalRooms;
    @Schema(description = "Whether the room type is active", example = "true")
    private Boolean active;
    @Schema(description = "Image path or URL for the room type image", example = "/api/images/room-types/room-type-1.jpg")
    private String imagePath;
    @Schema(description = "Names of amenities assigned to this room type")
    private Set<String> amenityNames;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHotelId() { return hotelId; }
    public void setHotelId(Long hotelId) { this.hotelId = hotelId; }
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
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
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public Set<String> getAmenityNames() { return amenityNames; }
    public void setAmenityNames(Set<String> amenityNames) { this.amenityNames = amenityNames; }
}