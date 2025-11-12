package com.amin.store.carts;

import com.amin.store.products.Category;
import com.amin.store.products.CategoryRepository;
import com.amin.store.products.Product;
import com.amin.store.products.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test the full flow: Create Cart -> Add Item.
     */
    @Test
    void addCartItem_ShouldAddItem_WhenCartAndProductExist() throws Exception {
        // 1. Setup Data
        Category cat = categoryRepository.save(new Category("Misc"));
        Product product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.TEN)
                .description("Desc")
                .category(cat)
                .build();
        productRepository.save(product);

        // 2. Create a Cart
        String cartResponse = mockMvc.perform(post("/carts"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CartDto cartDto = objectMapper.readValue(cartResponse, CartDto.class);

        // 3. Add Item to Cart
        AddItemToCartRequest request = new AddItemToCartRequest();
        request.setProductId(product.getId());

        mockMvc.perform(post("/carts/" + cartDto.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.product.id").value(product.getId()))
                .andExpect(jsonPath("$.quantity").value(1));
    }
}