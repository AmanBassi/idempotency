package com.example.idempotency.repository;

import com.example.idempotency.model.PaymentSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentSessionRepository
        extends MongoRepository<PaymentSession, String>, PaymentSessionRepositoryCustom {

    Optional<PaymentSession> findByIdempotencyKey(String idempotencyKey);
}
