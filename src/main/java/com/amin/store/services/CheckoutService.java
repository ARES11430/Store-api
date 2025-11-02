package com.amin.store.services;

import com.amin.store.dtos.CheckoutRequest;
import com.amin.store.dtos.CheckoutResponse;
import com.amin.store.entities.Order;
import com.amin.store.exceptions.CartEmptyException;
import com.amin.store.exceptions.CartNotFoundException;
import com.amin.store.repositories.CartRepository;
import com.amin.store.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;

    public CheckoutResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException();
        }

        if (cart.isEmpty()) {
            throw new CartEmptyException();
        }

        var order = Order.fromCart(cart, authService.getCurrentUser());

        orderRepository.save(order);

        cartService.clearCart(cart.getId());

        return new CheckoutResponse(order.getId());
    }
}