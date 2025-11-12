package com.amin.store.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CheckoutControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test that checkout fails with 400 Bad Request if the Cart ID is invalid/not found.
     * (Dependencies like PaymentGateway are mocked inside the Service test,
     * here we test the Controller routing and exception handling).
     */
    @Test
    @WithMockUser
    void checkout_ShouldReturnBadRequest_WhenCartNotFound() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setCartId(UUID.randomUUID()); // Random ID likely doesn't exist

        mockMvc.perform(post("/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Expecting CartNotFoundException mapped to 400
    }

    /**
     * Test validation: Cart ID cannot be null.
     */
    @Test
    @WithMockUser
    void checkout_ShouldFailValidation_WhenCartIdNull() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        // Cart ID is null by default

        mockMvc.perform(post("/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Validation error
    }
}