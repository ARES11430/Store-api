package com.amin.store.carts;

import com.amin.store.products.Product;
import com.amin.store.products.ProductNotFoundException;
import com.amin.store.products.ProductRepository;
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
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    /**
     * Test creating a new cart.
     */
    @Test
    void createCart_ShouldSaveAndReturnCart() {
        // Arrange
        Cart cart = new Cart();
        CartDto cartDto = new CartDto();
        cartDto.setId(UUID.randomUUID());

        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toDto(any(Cart.class))).thenReturn(cartDto);

        // Act
        CartDto result = cartService.createCart();

        // Assert
        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    /**
     * Test adding an item to an existing cart.
     */
    @Test
    void addToCart_ShouldAddItem_WhenCartAndProductExist() {
        // Arrange
        UUID cartId = UUID.randomUUID();
        Long productId = 1L;

        Cart cart = new Cart();
        cart.setId(cartId);

        Product product = new Product();
        product.setId(productId);
        product.setPrice(BigDecimal.TEN);

        CartItemDto expectedDto = new CartItemDto();
        expectedDto.setQuantity(1);

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toDto(any(CartItem.class))).thenReturn(expectedDto);

        // Act
        CartItemDto result = cartService.addToCart(cartId, productId);

        // Assert
        assertThat(result.getQuantity()).isEqualTo(1);
        verify(cartRepository).save(cart);
    }

    /**
     * Test adding item throws exception if cart missing.
     */
    @Test
    void addToCart_ShouldThrow_WhenCartNotFound() {
        UUID cartId = UUID.randomUUID();
        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(cartId, 1L))
                .isInstanceOf(CartNotFoundException.class);
    }

    /**
     * Test clearing a cart removes all items.
     */
    @Test
    void clearCart_ShouldRemoveAllItems() {
        // Arrange
        UUID cartId = UUID.randomUUID();
        Cart cart = new Cart();
        // Add a dummy item
        cart.addItem(new Product(1L, "P", "D", BigDecimal.ONE, null));

        when(cartRepository.getCartWithItems(cartId)).thenReturn(Optional.of(cart));

        // Act
        cartService.clearCart(cartId);

        // Assert
        assertThat(cart.getItems()).isEmpty();
        verify(cartRepository).save(cart);
    }
}