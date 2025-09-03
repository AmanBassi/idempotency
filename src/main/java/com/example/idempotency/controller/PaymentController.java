package com.example.idempotency.controller;

import com.example.idempotency.model.PaymentRequest;
import com.example.idempotency.model.PaymentSession;
import com.example.idempotency.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/session")
    public ResponseEntity<String> createPaymentSession(@RequestBody PaymentRequest request) {
        String cin = "1234567890"; // Normally comes from authenticated user
        PaymentSession session = paymentService.createPaymentSession(cin, request);
        return ResponseEntity.ok(session.getIdempotencyKey());
    }

    @PostMapping("/confirm")
    public Map<String, String> confirmPayment(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request
    ) {
        String cin = "1234567890";
        String result = paymentService.confirmPayment(idempotencyKey, cin, request);
        return Map.of("idempotencyKey", idempotencyKey, "result", result);
    }
}
