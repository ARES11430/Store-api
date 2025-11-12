package com.amin.store.payments;

import com.amin.store.auth.AuthService;
import com.amin.store.carts.Cart;
import com.amin.store.carts.CartEmptyException;
import com.amin.store.carts.CartRepository;
import com.amin.store.carts.CartService;
import com.amin.store.orders.Order;
import com.amin.store.orders.OrderRepository;
import com.amin.store.products.Product;
import com.amin.store.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private AuthService authService;
    @Mock private CartService cartService;
    @Mock private PaymentGateway paymentGateway;

    @InjectMocks
    private CheckoutService checkoutService;

    /**
     * Test successful checkout flow.
     */
    @Test
    void checkout_ShouldReturnResponse_WhenCartValid() {
        // Arrange
        UUID cartId = UUID.randomUUID();
        CheckoutRequest request = new CheckoutRequest();
        request.setCartId(cartId);

        User user = new User();
        user.setId(1L);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.addItem(new Product(1L, "Prod", "Desc", BigDecimal.TEN, null));

        Order order = new Order();
        order.setId(100L);

        CheckoutSession session = new CheckoutSession("http://checkout.url");

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(authService.getCurrentUser()).thenReturn(user);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(paymentGateway.createCheckoutSession(any(Order.class))).thenReturn(session);

        // Act
        CheckoutResponse response = checkoutService.checkout(request);

        // Assert
        assertThat(response.getCheckoutUrl()).isEqualTo("http://checkout.url");
        verify(cartService).clearCart(cartId); // Verify cart was cleared
    }

    /**
     * Test checkout throws exception for empty cart.
     */
    @Test
    void checkout_ShouldThrow_WhenCartEmpty() {
        UUID cartId = UUID.randomUUID();
        CheckoutRequest request = new CheckoutRequest();
        request.setCartId(cartId);

        Cart cart = new Cart(); // Empty items

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(CartEmptyException.class);
    }

    /**
     * Test that Order is DELETED if Payment Gateway throws an error.
     */
    @Test
    void checkout_ShouldDeleteOrder_WhenPaymentFails() {
        // Arrange
        UUID cartId = UUID.randomUUID();
        CheckoutRequest request = new CheckoutRequest();
        request.setCartId(cartId);

        Cart cart = new Cart();
        cart.setId(cartId);
        cart.addItem(new Product(1L, "P", "D", BigDecimal.TEN, null));

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(authService.getCurrentUser()).thenReturn(new User());
        when(paymentGateway.createCheckoutSession(any())).thenThrow(new PaymentException("Stripe Error"));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.checkout(request))
                .isInstanceOf(PaymentException.class);

        verify(orderRepository).delete(any(Order.class)); // Ensure rollback happened
    }
}