package com.amin.store.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Uses application-test.yaml
@Transactional // Rolls back DB changes after each test
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Category savedCategory;

    @BeforeEach
    void setUp() {
        // Seed database with a category for products
        Category category = new Category();
        category.setName("Electronics");
        savedCategory = categoryRepository.save(category);
    }

    /**
     * Test that anyone (even unauthenticated) can view products.
     */
    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        // Arrange
        Product product = Product.builder()
                .name("Test Phone")
                .description("Description")
                .price(BigDecimal.valueOf(100.00))
                .category(savedCategory)
                .build();
        productRepository.save(product);

        // Act & Assert
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Phone"));
    }

    /**
     * Test that an ADMIN can create a product.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createProduct_ShouldSucceed_WhenAdmin() throws Exception {
        // Arrange
        ProductDto productDto = new ProductDto();
        productDto.setName("New Laptop");
        productDto.setPrice(BigDecimal.valueOf(999.99));
        productDto.setDescription("Powerful laptop");
        productDto.setCategoryId(savedCategory.getId());

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Laptop"));
    }

    /**
     * Test that a regular USER cannot create a product (403 Forbidden).
     */
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void createProduct_ShouldFail_WhenNotAdmin() throws Exception {
        // Arrange
        ProductDto productDto = new ProductDto();
        productDto.setName("Hacker Laptop");
        productDto.setPrice(BigDecimal.valueOf(10));
        productDto.setCategoryId(savedCategory.getId());

        // Act & Assert
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isForbidden());
    }
}