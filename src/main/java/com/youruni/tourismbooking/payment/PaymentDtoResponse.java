package com.youruni.tourismbooking.payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public class PaymentDtoResponse {
    private Long id;
    private Long bookingId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public PaymentDtoResponse() {
    }
    public PaymentDtoResponse(Long id, Long bookingId, BigDecimal amount, PaymentStatus status) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
    }
    public PaymentDtoResponse(Long id, Long bookingId, BigDecimal amount, PaymentStatus status,
                              String paymentMethod, String transactionReference, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.createdAt = createdAt;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getBookingId() {
        return bookingId;
    }
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    public String getTransactionReference() {
        return transactionReference;
    }
    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
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