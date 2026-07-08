package com.tiffin.service;

import com.tiffin.dto.InitiatePaymentRequest;
import com.tiffin.dto.InitiatePaymentResponse;
import com.tiffin.dto.PaymentResponse;

import java.util.List;
import java.util.Map;

public interface PaymentService {

    // Customer initiates payment for an order
    InitiatePaymentResponse initiatePayment(String userId,
                                            InitiatePaymentRequest request, String email);

    // Handle webhook from Razorpay
    void handleWebhook(String payload, String razorpaySignature);

    // Get payment details by our internal payment ID
    PaymentResponse getPaymentById(String paymentId);

    // Get payment by order ID
    PaymentResponse getPaymentByOrderId(String orderId);

    // Customer's payment history
    List<PaymentResponse> getMyPayments(String userId);
}