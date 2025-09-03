package com.example.idempotency.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payment_sessions")
public class PaymentSession {
    @Id
    private String id;
    @Indexed(unique = true)
    private String idempotencyKey;
    private String cin;
    private String paymentRequestHash;
    private String responseBody;
    private String status;
    private Instant createdAt = Instant.now();

    public PaymentSession(String idempotencyKey, String cin, String paymentRequestHash, String status) {
        this.idempotencyKey = idempotencyKey;
        this.cin = cin;
        this.paymentRequestHash = paymentRequestHash;
        this.status = status;
    }
}
