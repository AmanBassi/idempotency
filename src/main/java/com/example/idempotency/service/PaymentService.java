package com.example.idempotency.service;

import com.example.idempotency.model.PaymentRequest;
import com.example.idempotency.model.PaymentSession;
import com.example.idempotency.repository.PaymentSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentSessionRepository paymentSessionRepository;

    public PaymentSession createPaymentSession(String cin, PaymentRequest paymentRequest) {
        String key = UUID.randomUUID().toString();
        String requestHash = String.valueOf(paymentRequest.hashCode());

        PaymentSession session = new PaymentSession(key, cin, requestHash, "PENDING");

        try {
            return paymentSessionRepository.save(session);
        } catch (DuplicateKeyException e) {
            return paymentSessionRepository.findByIdempotencyKey(key).orElseThrow(() -> e);
        }
    }

    @Transactional
    public String confirmPayment(String idempotencyKey, String cin, PaymentRequest paymentRequest) {
        String requestHash = String.valueOf(paymentRequest.hashCode());

        // Atomically update the status from PENDING to PROCESSING
        PaymentSession session = paymentSessionRepository.updateStatusToProcessing(idempotencyKey, cin, requestHash);

        // If the atomic update failed, another thread has already processed or is processing this key.
        if (session == null) {
            // Retrieve the session to check its current status
            PaymentSession existing = paymentSessionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Invalid or expired idempotency key"));

            return switch (existing.getStatus()) {
                case "COMPLETED" -> existing.getResponseBody();
                case "PROCESSING" -> "Payment is already processing, try later.";
                case "FAILED" -> throw new IllegalStateException("Previous payment attempt failed");
                default -> throw new IllegalStateException("Unexpected state: " + existing.getStatus());
            };
        }

        if (!session.getCin().equals(cin)) {
            throw new SecurityException("Key does not belong to this user");
        }

        if (!session.getPaymentRequestHash().equals(requestHash)) {
            throw new IllegalStateException("Payment details do not match original session");
        }

        try {
            // Core payment processing logic here
            String paymentResponse = "Payment logic here key:" + session.getId();

            session.setStatus("COMPLETED");
            session.setResponseBody(paymentResponse);
            paymentSessionRepository.save(session);

            return paymentResponse;
        } catch (Exception e) {
            session.setStatus("FAILED");
            paymentSessionRepository.save(session);
            throw e;
        }
    }
}
