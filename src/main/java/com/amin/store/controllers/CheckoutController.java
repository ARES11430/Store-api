package com.amin.store.controllers;

import com.amin.store.dtos.CheckoutRequest;
import com.amin.store.dtos.CheckoutResponse;
import com.amin.store.dtos.ErrorDto;
import com.amin.store.exceptions.CartEmptyException;
import com.amin.store.exceptions.CartNotFoundException;

import com.amin.store.exceptions.PaymentException;
import com.amin.store.services.CheckoutService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.net.Webhook;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
public class CheckoutController {
    private final CheckoutService checkoutService;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @PostMapping
    public CheckoutResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<?> handlePaymentException() {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader("Stripe-Signature") String signature,
            @RequestBody String payload
    ) {
        try {
            var event = Webhook.constructEvent(payload, signature, webhookSecretKey);
            System.out.println(event.getType());

            var stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);

            switch (event.getType()) {
                case "payment_intent.succeeded" -> {
                    // Update order status (PAID)
                }
                case "payment_intent.failed" -> {
                    // Update order status (FAILED)
                }
            }

            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @ExceptionHandler({CartNotFoundException.class, CartEmptyException.class})
    public ResponseEntity<ErrorDto> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorDto(ex.getMessage()));
    }
}
