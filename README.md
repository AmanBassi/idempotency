Idempotency Payment Service
This is a simple Spring Boot application that demonstrates how to implement an idempotent payment service. Idempotency is a crucial concept in distributed systems, ensuring that repeated requests have the same effect as a single request. This is particularly important for financial transactions to prevent duplicate payments caused by network issues or client retries.

The project uses MongoDB for data persistence and a combination of an atomic database operation (findAndModify) and a unique key to handle concurrent requests and prevent race conditions.

Project Structure
com.example.idempotency.controller.PaymentController: The REST controller that exposes the API endpoints for creating and confirming payments.

com.example.idempotency.service.PaymentService: The core business logic for handling payment sessions and processing payments. This is where the idempotency logic resides.

com.example.idempotency.repository.PaymentSessionRepository: The Spring Data MongoDB repository for interacting with the PaymentSession collection.

com.example.idempotency.model.PaymentSession: The domain model representing a payment session. It includes the idempotencyKey with a unique index to enforce a single session per key.

Key Concepts
Idempotency Key: A unique, client-generated key used to identify a single payment attempt. The client should send this key with every request.

Atomic Operations: Using MongoDB's findAndModify command to atomically update the payment session status from PENDING to PROCESSING. This is the cornerstone of the solution, as it prevents two concurrent requests from processing the same payment.

State Machine: The PaymentSession uses a simple state machine with statuses like PENDING, PROCESSING, COMPLETED, and FAILED to track the state of a transaction.

API Endpoints
The API exposes two primary endpoints:

1. Create Payment Session
Endpoint: POST /api/payments/session

Request Body:

{
  "amount": 100.0,
  "currency": "USD"
}

Response:

Returns the idempotencyKey that the client should use for the subsequent confirm request.

Example: {"idempotencyKey": "f6b3e71d-a5c9-4f7f-8c3b-7f28f8f2b3b7"}

2. Confirm Payment
Endpoint: POST /api/payments/confirm

Headers: Idempotency-Key: [the key from the create session call]

Request Body:

{
  "amount": 100.0,
  "currency": "USD"
}

Response:

If the payment is successful: a JSON object with the idempotencyKey and the result of the payment.

If the request is a retry of a successful payment, the original response is returned.

If the request is a retry of a payment still in progress, a "Payment is already processing" message is returned.

If the request is for an invalid key or a payment that failed, an error is returned.

Setup and Running the Application
Prerequisites:

Java 17 or higher

Maven

MongoDB running on localhost:27017

Clone the Repository:

git clone <your-repository-url>
cd idempotency-service

Run the Application:

./mvnw spring-boot:run

The application will start on http://localhost:8080.

How to Test
You can use a tool like curl or Postman to test the endpoints.

Step 1: Create a session to get the key.

curl -X POST http://localhost:8080/api/payments/session \
-H "Content-Type: application/json" \
-d '{"amount":10.50, "currency":"USD"}'

Step 2: Confirm the payment using the returned key.

curl -X POST http://localhost:8080/api/payments/confirm \
-H "Idempotency-Key: <paste_the_key_here>" \
-H "Content-Type: application/json" \
-d '{"amount":10.50, "currency":"USD"}'

Step 3: Test Idempotency: Repeat the confirm request with the same Idempotency-Key to see that it returns the same response without processing a duplicate payment.
