package com.youruni.tourismbooking.catalog.amenity;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(description = "Response object for amenity data")
public class AmenityDtoResponse {
    @Schema(description = "Amenity ID", example = "1")
    private Long id;
    @Schema(description = "Amenity name", example = "Swimming Pool")
    private String name;
    @Schema(description = "Amenity description", example = "Olympic-sized outdoor swimming pool")
    private String description;
    @Schema(description = "Amenity type", example = "ENTERTAINMENT")
    private AmenityType type;
    @Schema(description = "Amenity scope", example = "HOTEL")
    private AmenityScope scope;
    @Schema(description = "Whether the amenity is active", example = "true")
    private Boolean active;
    @Schema(description = "Timestamp when amenity was created")
    private LocalDateTime createdAt;
    @Schema(description = "Timestamp when amenity was last updated")
    private LocalDateTime updatedAt;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
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
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}