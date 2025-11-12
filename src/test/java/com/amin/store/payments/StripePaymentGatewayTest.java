package com.amin.store.payments;

import com.amin.store.orders.Order;
import com.amin.store.orders.OrderItem;
import com.amin.store.orders.PaymentStatus;
import com.amin.store.products.Product;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripePaymentGatewayTest {

    @InjectMocks
    private StripePaymentGateway stripePaymentGateway;

    /**
     * Test creating a checkout session.
     * We mock the static Session.create() method.
     */
    @Test
    void createCheckoutSession_ShouldReturnSessionUrl() throws StripeException {
        // Inject properties manually since we are not loading Spring Context
        ReflectionTestUtils.setField(stripePaymentGateway, "websiteUrl", "http://localhost");

        // Arrange
        Order order = new Order();
        order.setId(100L);

        Product product = new Product();
        product.setName("Test Product");

        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);

        order.setItems(Set.of(item));

        Session sessionMock = mock(Session.class);
        when(sessionMock.getUrl()).thenReturn("https://stripe.com/checkout/session");

        // Act
        // Try-with-resources to ensure the static mock is closed after the block
        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(sessionMock);

            CheckoutSession result = stripePaymentGateway.createCheckoutSession(order);

            // Assert
            assertThat(result.getCheckoutUrl()).isEqualTo("https://stripe.com/checkout/session");
        }
    }

    /**
     * Test handling a payment success webhook.
     */
    @Test
    void parseWebhookRequest_ShouldReturnPaidStatus_WhenEventIsSuccess() throws SignatureVerificationException {
        ReflectionTestUtils.setField(stripePaymentGateway, "webhookSecretKey", "secret");

        WebhookRequest request = new WebhookRequest(
                Map.of("stripe-signature", "sig"), "payload"
        );

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.succeeded");

        // Mock the nested structure: Event -> DataObjectDeserializer -> StripeObject (PaymentIntent)
        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        when(paymentIntent.getMetadata()).thenReturn(Map.of("order_id", "555"));

        // We need to cast the PaymentIntent to StripeObject for the mock to work with the interface
        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent("payload", "sig", "secret"))
                    .thenReturn(event);

            Optional<PaymentResult> result = stripePaymentGateway.parseWebhookRequest(request);

            assertThat(result).isPresent();
            assertThat(result.get().getOrderId()).isEqualTo(555L);
            assertThat(result.get().getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }
    }

    /**
     * Test handling a payment failed webhook.
     */
    @Test
    void parseWebhookRequest_ShouldReturnFailedStatus_WhenEventIsFailure() throws SignatureVerificationException {
        ReflectionTestUtils.setField(stripePaymentGateway, "webhookSecretKey", "secret");

        WebhookRequest request = new WebhookRequest(
                Map.of("stripe-signature", "sig"), "payload"
        );

        Event event = mock(Event.class);
        when(event.getType()).thenReturn("payment_intent.payment_failed");

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        PaymentIntent paymentIntent = mock(PaymentIntent.class);
        when(paymentIntent.getMetadata()).thenReturn(Map.of("order_id", "555"));

        when(deserializer.getObject()).thenReturn(Optional.of(paymentIntent));

        try (MockedStatic<Webhook> mockedWebhook = mockStatic(Webhook.class)) {
            mockedWebhook.when(() -> Webhook.constructEvent(any(), any(), any()))
                    .thenReturn(event);

            Optional<PaymentResult> result = stripePaymentGateway.parseWebhookRequest(request);

            assertThat(result).isPresent();
            assertThat(result.get().getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }
}