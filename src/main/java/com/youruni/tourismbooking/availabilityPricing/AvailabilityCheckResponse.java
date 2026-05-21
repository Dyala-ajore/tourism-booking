package com.youruni.tourismbooking.availabilityPricing;
import java.math.BigDecimal;
public class AvailabilityCheckResponse {
    private boolean available;
    private BigDecimal totalPrice;
    private long nights;
    public AvailabilityCheckResponse(boolean available, BigDecimal totalPrice, long nights) {
        this.available = available;
        this.totalPrice = totalPrice;
        this.nights = nights;
    }
    public boolean isAvailable() {
        return available;
    }
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    public long getNights() {
        return nights;
    }
}