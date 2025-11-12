package com.amin.store.orders;

import com.amin.store.auth.AuthService;
import com.amin.store.users.User;
import com.amin.store.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;

    // FIX: Mock AuthService to bypass the Principal casting logic
    @MockitoBean
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user in DB
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@order.com");
        testUser.setPassword("pass");
        testUser = userRepository.save(testUser);

        // FIX: Stub getCurrentUser to return our test user
        when(authService.getCurrentUser()).thenReturn(testUser);
    }

    /**
     * Test getting a list of orders for the authenticated user.
     */
    @Test
    @WithMockUser(username = "test@order.com")
    void getAllOrders_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk());
    }

    /**
     * Test getting a specific order.
     */
    @Test
    @WithMockUser(username = "test@order.com")
    void getOrder_ShouldReturnOk_WhenExists() throws Exception {
        // Create an order for this user
        Order order = new Order();
        order.setCustomer(testUser);
        order.setStatus(PaymentStatus.PENDING);
        order.setTotalPrice(BigDecimal.TEN);
        orderRepository.save(order);

        mockMvc.perform(get("/orders/" + order.getId()))
                .andExpect(status().isOk());
    }

    /**
     * Test getting a non-existent order returns 404.
     */
    @Test
    @WithMockUser(username = "test@order.com")
    void getOrder_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/orders/999999"))
                .andExpect(status().isNotFound());
    }
}