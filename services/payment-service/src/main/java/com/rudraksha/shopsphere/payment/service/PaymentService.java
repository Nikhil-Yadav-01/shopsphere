package com.rudraksha.shopsphere.payment.service;

import com.rudraksha.shopsphere.payment.dto.request.CreatePaymentRequest;
import com.rudraksha.shopsphere.payment.dto.request.RefundRequest;
import com.rudraksha.shopsphere.payment.dto.response.PaymentResponse;
import com.rudraksha.shopsphere.payment.dto.response.RefundResponse;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse getPayment(UUID paymentId);

    List<PaymentResponse> getPaymentsByOrderId(UUID orderId);

    RefundResponse processRefund(UUID paymentId, RefundRequest request);

    void handleWebhookEvent(String payload, String signature);
}
