package com.ecom.payment.service;

import com.ecom.payment.dto.PaymentRequest;
import com.ecom.payment.dto.PaymentResponse;
import com.ecom.payment.dto.RefundRequest;
import com.ecom.payment.exception.PaymentNotFoundException;
import com.ecom.payment.exception.PaymentProcessingException;
import com.ecom.payment.model.Payment;
import com.ecom.payment.model.PaymentStatus;
import com.ecom.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Random random;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.random = new Random();
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setUserId(request.userId());
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod() != null ? request.paymentMethod() : "MOCK_CARD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId(UUID.randomUUID().toString());
        payment.setCreatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // Mock payment processing: 90% success, 10% failure
        boolean isSuccess = random.nextInt(10) < 9;
        payment.setStatus(isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment = paymentRepository.save(payment);

        String message = isSuccess ? "Payment processed successfully" : "Payment processing failed";
        return toPaymentResponse(payment, message);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
        return toPaymentResponse(payment, "Payment retrieved successfully");
    }

    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order id: " + orderId));
        return toPaymentResponse(payment, "Payment retrieved successfully");
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream()
                .map(payment -> toPaymentResponse(payment, "Payment retrieved successfully"))
                .collect(Collectors.toList());
    }

    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentProcessingException("Payment has already been refunded");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentProcessingException("Only successful payments can be refunded");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        String message = "Payment refunded successfully" +
                (request.reason() != null ? ". Reason: " + request.reason() : "");
        return toPaymentResponse(payment, message);
    }

    private PaymentResponse toPaymentResponse(Payment payment, String message) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getUserId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getTransactionId(),
                message,
                payment.getCreatedAt()
        );
    }
}
