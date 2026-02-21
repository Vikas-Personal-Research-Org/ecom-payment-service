package com.ecom.payment.service;

import com.ecom.payment.dto.PaymentRequest;
import com.ecom.payment.dto.PaymentResponse;
import com.ecom.payment.dto.RefundRequest;
import com.ecom.payment.exception.PaymentNotFoundException;
import com.ecom.payment.exception.PaymentProcessingException;
import com.ecom.payment.model.Payment;
import com.ecom.payment.model.PaymentStatus;
import com.ecom.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment(1L, 1L, new BigDecimal("59.98"), "MOCK_CARD",
                PaymentStatus.SUCCESS, "txn-123", LocalDateTime.now());
        payment.setId(1L);
    }

    @Test
    void processPayment_ShouldCreatePayment() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("59.98"), "MOCK_CARD");

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals(1L, response.userId());
        assertEquals(new BigDecimal("59.98"), response.amount());
        assertNotNull(response.transactionId());
        assertTrue(response.status() == PaymentStatus.SUCCESS || response.status() == PaymentStatus.FAILED);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void processPayment_ShouldUseDefaultPaymentMethod_WhenNull() {
        PaymentRequest request = new PaymentRequest(1L, 1L, new BigDecimal("59.98"), null);

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void getPaymentById_ShouldReturnPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(1L, response.orderId());
        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals("txn-123", response.transactionId());
    }

    @Test
    void getPaymentById_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentById(99L));
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentByOrderId(1L);

        assertNotNull(response);
        assertEquals(1L, response.orderId());
    }

    @Test
    void getPaymentByOrderId_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPaymentByOrderId(99L));
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() {
        when(paymentRepository.findByUserId(1L)).thenReturn(List.of(payment));

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).userId());
    }

    @Test
    void getPaymentsByUserId_ShouldReturnEmptyList_WhenNoPayments() {
        when(paymentRepository.findByUserId(99L)).thenReturn(List.of());

        List<PaymentResponse> responses = paymentService.getPaymentsByUserId(99L);

        assertTrue(responses.isEmpty());
    }

    @Test
    void refundPayment_ShouldRefundSuccessfulPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundRequest request = new RefundRequest(1L, "Customer requested");
        PaymentResponse response = paymentService.refundPayment(1L, request);

        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.status());
        assertTrue(response.message().contains("refunded"));
        assertTrue(response.message().contains("Customer requested"));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenAlreadyRefunded() {
        payment.setStatus(PaymentStatus.REFUNDED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RefundRequest request = new RefundRequest(1L, "Duplicate refund");
        assertThrows(PaymentProcessingException.class,
                () -> paymentService.refundPayment(1L, request));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenPaymentFailed() {
        payment.setStatus(PaymentStatus.FAILED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        RefundRequest request = new RefundRequest(1L, "Failed payment");
        assertThrows(PaymentProcessingException.class,
                () -> paymentService.refundPayment(1L, request));
    }

    @Test
    void refundPayment_ShouldThrowException_WhenNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        RefundRequest request = new RefundRequest(99L, "Not found");
        assertThrows(PaymentNotFoundException.class,
                () -> paymentService.refundPayment(99L, request));
    }

    @Test
    void refundPayment_ShouldHandleNullReason() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefundRequest request = new RefundRequest(1L, null);
        PaymentResponse response = paymentService.refundPayment(1L, request);

        assertNotNull(response);
        assertEquals(PaymentStatus.REFUNDED, response.status());
    }
}
