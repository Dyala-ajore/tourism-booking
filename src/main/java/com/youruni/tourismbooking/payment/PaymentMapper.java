package com.youruni.tourismbooking.payment;
import org.springframework.stereotype.Component;
@Component
public class PaymentMapper {
    public PaymentDtoResponse toResponseDto(Payment payment) {
        if (payment == null) {
            return null;
        }
        PaymentDtoResponse response = new PaymentDtoResponse();
        response.setId(payment.getId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setTransactionReference(payment.getTransactionReference());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        if (payment.getBooking() != null) {
            response.setBookingId(payment.getBooking().getId());
        }
        return response;
    }
    public Payment toEntity(PaymentDtoRequest requestDto) {
        if (requestDto == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setAmount(requestDto.getAmount());
        payment.setPaymentMethod(requestDto.getPaymentMethod());
        payment.setTransactionReference(requestDto.getTransactionReference());
        payment.setStatus(PaymentStatus.INITIATED);
        return payment;
    }
    public void updateEntityFromDto(PaymentDtoRequest requestDto, Payment payment) {
        if (requestDto == null || payment == null) {
            return;
        }
        if (requestDto.getAmount() != null) {
            payment.setAmount(requestDto.getAmount());
        }
        if (requestDto.getPaymentMethod() != null) {
            payment.setPaymentMethod(requestDto.getPaymentMethod());
        }
        if (requestDto.getTransactionReference() != null) {
            payment.setTransactionReference(requestDto.getTransactionReference());
        }
    }
}