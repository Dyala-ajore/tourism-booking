package com.youruni.tourismbooking.availabilityPricing;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
@Service
public class PricingService {
    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.25");  
    private static final BigDecimal WEEKDAY_MULTIPLIER = BigDecimal.ONE;           
    public BigDecimal calculateTotalPrice(LocalDate checkInDate, LocalDate checkOutDate, BigDecimal pricePerNight) {
        if (checkInDate.isAfter(checkOutDate) || checkInDate.isEqual(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        if (pricePerNight == null) {
            throw new IllegalArgumentException("Price per night cannot be null");
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        LocalDate currentDate = checkInDate;
        while (currentDate.isBefore(checkOutDate)) {
            BigDecimal nightPrice = applyDayOfWeekMultiplier(pricePerNight, currentDate);
            totalPrice = totalPrice.add(nightPrice);
            currentDate = currentDate.plusDays(1);
        }
        return totalPrice;
    }
    public long calculateNumberOfNights(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate.isAfter(checkOutDate) || checkInDate.isEqual(checkOutDate)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }
        return java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    public BigDecimal applyDynamicPricing(BigDecimal basePrice, BigDecimal adjustmentPercentage) {
        if (basePrice == null) {
            throw new IllegalArgumentException("Base price cannot be null");
        }
        if (adjustmentPercentage == null || adjustmentPercentage.signum() == 0) {
            return basePrice;
        }
        BigDecimal multiplier = BigDecimal.ONE.add(adjustmentPercentage.divide(BigDecimal.valueOf(100)));
        return basePrice.multiply(multiplier);
    }
    public BigDecimal applyDayOfWeekMultiplier(BigDecimal basePrice, LocalDate date) {
        if (basePrice == null) {
            throw new IllegalArgumentException("Base price cannot be null");
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return basePrice.multiply(WEEKEND_MULTIPLIER).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return basePrice.multiply(WEEKDAY_MULTIPLIER).setScale(2, java.math.RoundingMode.HALF_UP);
    }
    public boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}