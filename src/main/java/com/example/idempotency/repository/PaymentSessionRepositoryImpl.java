package com.example.idempotency.repository;

import com.example.idempotency.model.PaymentSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentSessionRepositoryImpl implements PaymentSessionRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public PaymentSession updateStatusToProcessing(String idempotencyKey, String cin, String paymentRequestHash) {
        Query query = new Query(Criteria.where("idempotencyKey").is(idempotencyKey)
                .and("status").is("PENDING")
                .and("cin").is(cin)
                .and("paymentRequestHash").is(paymentRequestHash));

        Update update = new Update().set("status", "PROCESSING");

        return mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                PaymentSession.class
        );
    }
}
