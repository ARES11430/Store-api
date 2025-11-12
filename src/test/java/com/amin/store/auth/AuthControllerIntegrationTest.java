package com.amin.store.auth;

import com.amin.store.users.RegisterUserRequest;
import com.amin.store.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create a user directly in DB for login test
        var userRequest = new RegisterUserRequest();
        userRequest.setEmail("existing@test.com");
        userRequest.setPassword("password123");
        userRequest.setName("Existing User");

        // We rely on the existing service or manual repo save
        com.amin.store.users.User user = new com.amin.store.users.User();
        user.setEmail("existing@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setName("Existing User");
        user.setRole(com.amin.store.users.Role.USER);
        userRepository.save(user);
    }

    /**
     * Test successful login returning JWT token.
     */
    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("existing@test.com");
        loginRequest.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty()); // Checks Access Token
    }

    /**
     * Test login failure with wrong password.
     */
    @Test
    void login_ShouldFail_WhenPasswordIsInvalid() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("existing@test.com");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}