package com.ecom.payment.controller;

import com.ecom.payment.dto.PaymentRequest;
import com.ecom.payment.dto.PaymentResponse;
import com.ecom.payment.dto.RefundRequest;
import com.ecom.payment.exception.PaymentNotFoundException;
import com.ecom.payment.model.PaymentStatus;
import com.ecom.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        paymentResponse = new PaymentResponse(1L, 1L, 1L, new BigDecimal("59.98"),
                PaymentStatus.SUCCESS, "txn-123", "Payment processed successfully", LocalDateTime.now());
    }

    @Test
    void processPayment_ShouldReturn201() throws Exception {
        when(paymentService.processPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("59.98"), "MOCK_CARD");
        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId").value("txn-123"));
    }

    @Test
    void processPayment_ShouldReturn400_WhenValidationFails() throws Exception {
        // orderId is null - should fail validation
        String invalidRequest = "{\"userId\": 1, \"amount\": 59.98}";
        mockMvc.perform(post("/api/payments/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentById_ShouldReturnPayment() throws Exception {
        when(paymentService.getPaymentById(1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void getPaymentById_ShouldReturn404_WhenNotFound() throws Exception {
        when(paymentService.getPaymentById(99L)).thenThrow(new PaymentNotFoundException("Payment not found"));

        mockMvc.perform(get("/api/payments/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() throws Exception {
        when(paymentService.getPaymentByOrderId(1L)).thenReturn(paymentResponse);

        mockMvc.perform(get("/api/payments/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(1));
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() throws Exception {
        when(paymentService.getPaymentsByUserId(1L)).thenReturn(List.of(paymentResponse));

        mockMvc.perform(get("/api/payments/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void getPaymentsByUserId_ShouldReturnEmptyList() throws Exception {
        when(paymentService.getPaymentsByUserId(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/payments/user/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void refundPayment_ShouldReturnRefundedPayment() throws Exception {
        PaymentResponse refundResponse = new PaymentResponse(1L, 1L, 1L, new BigDecimal("59.98"),
                PaymentStatus.REFUNDED, "txn-123", "Payment refunded successfully", LocalDateTime.now());
        when(paymentService.refundPayment(eq(1L), any(RefundRequest.class))).thenReturn(refundResponse);

        RefundRequest request = new RefundRequest(1L, "Customer requested");
        mockMvc.perform(post("/api/payments/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
