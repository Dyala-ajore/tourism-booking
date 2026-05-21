package com.youruni.tourismbooking.payment;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public class PaymentDtoRequest {
    @NotNull(message = "Booking ID cannot be null")
    @Positive(message = "Booking ID must be positive")
    private Long bookingId;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotBlank(message = "Payment method cannot be blank")
    @Pattern(regexp = "CREDIT_CARD|DEBIT_CARD|NET_BANKING|UPI|WALLET",
            message = "Payment method must be one of: CREDIT_CARD, DEBIT_CARD, NET_BANKING, UPI, WALLET")
    private String paymentMethod;
    private String transactionReference;
    private Boolean simulateSuccess;
    public PaymentDtoRequest() {
    }
    public PaymentDtoRequest(Long bookingId, BigDecimal amount, String paymentMethod) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
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
    public Boolean getSimulateSuccess() {
        return simulateSuccess;
    }
    public void setSimulateSuccess(Boolean simulateSuccess) {
        this.simulateSuccess = simulateSuccess;
    }
}