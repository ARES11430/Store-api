package com.amin.store.orders;

import com.amin.store.auth.AuthService;
import com.amin.store.users.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private AuthService authService;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    /**
     * Test fetching an order that belongs to the user.
     */
    @Test
    void getOrder_ShouldReturnOrder_WhenOwnedByUser() {
        // Arrange
        Long orderId = 1L;
        User user = new User();
        user.setId(10L);

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(user);

        when(orderRepository.getOrderWithItems(orderId)).thenReturn(Optional.of(order));
        when(authService.getCurrentUser()).thenReturn(user);
        when(orderMapper.toDto(order)).thenReturn(new OrderDto());

        // Act
        OrderDto result = orderService.getOrder(orderId);

        // Assert
        assertThat(result).isNotNull();
    }

    /**
     * Test fetching an order belonging to someone else throws AccessDenied.
     */
    @Test
    void getOrder_ShouldThrowAccessDenied_WhenNotOwner() {
        // Arrange
        Long orderId = 1L;
        User currentUser = new User();
        currentUser.setId(10L);

        User otherUser = new User();
        otherUser.setId(99L); // Different ID

        Order order = new Order();
        order.setId(orderId);
        order.setCustomer(otherUser); // Order belongs to otherUser

        when(orderRepository.getOrderWithItems(orderId)).thenReturn(Optional.of(order));
        when(authService.getCurrentUser()).thenReturn(currentUser);

        // Act & Assert
        assertThatThrownBy(() -> orderService.getOrder(orderId))
                .isInstanceOf(AccessDeniedException.class);
    }
}