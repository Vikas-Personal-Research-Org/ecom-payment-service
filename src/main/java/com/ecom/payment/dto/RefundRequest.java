package com.ecom.payment.dto;

import jakarta.validation.constraints.NotNull;

public record RefundRequest(
        @NotNull(message = "Payment ID is required")
        Long paymentId,

        String reason
) {
}
