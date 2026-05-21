package com.youruni.tourismbooking.catalog.amenity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
@Schema(description = "Request object for creating/updating an amenity")
public class AmenityDtoRequest {
    @Schema(description = "Amenity name", example = "Swimming Pool")
    @NotBlank(message = "Amenity name cannot be blank")
    @Size(min = 2, max = 100, message = "Amenity name must be between 2 and 100 characters")
    private String name;
    @Schema(description = "Amenity description", example = "Olympic-sized outdoor swimming pool available year-round")
    @Size(max = 1000, message = "Amenity description cannot exceed 1000 characters")
    private String description;
    @Schema(description = "Amenity type", example = "ENTERTAINMENT")
    @NotNull(message = "Amenity type is required")
    private AmenityType type;
    @Schema(description = "Amenity scope (HOTEL, ROOM_TYPE, or BOTH)", example = "HOTEL")
    @NotNull(message = "Amenity scope is required")
    private AmenityScope scope;
    @Schema(description = "Whether the amenity is active", example = "true")
    private Boolean active = true;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public AmenityType getType() {
        return type;
    }
    public void setType(AmenityType type) {
        this.type = type;
    }
    public AmenityScope getScope() {
        return scope;
    }
    public void setScope(AmenityScope scope) {
        this.scope = scope;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}