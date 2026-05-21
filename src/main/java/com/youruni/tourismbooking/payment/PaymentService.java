package com.youruni.tourismbooking.payment;
public interface PaymentService {
    PaymentDtoResponse processPayment(PaymentDtoRequest request, String authenticatedUsername, String userRole);
    PaymentDtoResponse getPaymentById(Long id, String authenticatedUsername, String userRole);
    java.util.List<PaymentDtoResponse> getPaymentsByBookingId(Long bookingId, String authenticatedUsername, String userRole);
    PaymentDtoResponse refundPayment(Long paymentId, String authenticatedUsername, String userRole);
    PaymentDtoResponse refundPaymentByBookingId(Long bookingId, String authenticatedUsername, String userRole);
}