package com.example.idempotency.repository;

import com.example.idempotency.model.PaymentSession;

public interface PaymentSessionRepositoryCustom {
    PaymentSession updateStatusToProcessing(String idempotencyKey, String cin, String paymentRequestHash);
}
