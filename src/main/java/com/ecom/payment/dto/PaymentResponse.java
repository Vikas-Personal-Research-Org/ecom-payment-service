package com.ecom.payment.dto;

import com.ecom.payment.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentStatus status,
        String transactionId,
        String message,
        LocalDateTime createdAt
) {
}
