package com.ecom.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        Long orderId,

        @NotNull(message = "User ID is required")
        Long userId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        String paymentMethod
) {
}
