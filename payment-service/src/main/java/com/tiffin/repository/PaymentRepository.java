package com.tiffin.repository;

import com.tiffin.entity.Payment;
import com.tiffin.common.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    // Find by our internal order ID
    Optional<Payment> findByOrderId(String orderId);

    // Find by Razorpay order ID — used in webhook handling
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    // Idempotency check — has this Razorpay payment already been processed?
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);

    // Customer's payment history
    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    // Find by status
    List<Payment> findByStatus(PaymentStatus status);
}