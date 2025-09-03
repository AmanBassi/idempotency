package com.example.idempotency.model;

import lombok.Data;

@Data
public class PaymentRequest {
    private double amount;
    private String currency;
}
