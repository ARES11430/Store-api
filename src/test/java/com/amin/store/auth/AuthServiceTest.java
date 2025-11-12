package com.amin.store.auth;

import com.amin.store.users.Role;
import com.amin.store.users.User;
import com.amin.store.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Clear context before each test to avoid pollution
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Test that login authenticates via manager and generates tokens.
     */
    @Test
    void login_ShouldReturnTokens_WhenCredentialsAreValid() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("test@test.com");
        request.setPassword("pass");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setRole(Role.USER);

        Jwt accessToken = mock(Jwt.class);
        Jwt refreshToken = mock(Jwt.class);

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    /**
     * Test extracting the current user from SecurityContextHolder.
     */
    @Test
    void getCurrentUser_ShouldReturnUser_WhenAuthenticated() {
        // Arrange
        // We pick a random User ID (123) and say "Imagine this user is logged in."
        Long userId = 123L;

        // We create a fake "Security Badge" (Authentication) and stamp it with ID 123.
        Authentication auth = mock(Authentication.class);

        SecurityContext securityContext = mock(SecurityContext.class);

        //We put that badge in the global security holder (SecurityContextHolder).
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userId); // Principal is Long (userId) based on JwtAuthenticationFilter
        SecurityContextHolder.setContext(securityContext);

        // We tell the fake Database (userRepository): "If anyone asks for User 123, give them this user object."
        User expectedUser = new User();
        expectedUser.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = authService.getCurrentUser();

        // Assert
        assertThat(result).isEqualTo(expectedUser);
    }

    /**
     * Test token refresh flow.
     */
    @Test
    void refreshAccessToken_ShouldReturnNewToken_WhenRefreshTokenIsValid() {
        // Arrange
        // We have a "Refresh Token" string.
        String refreshTokenString = "valid_refresh_token";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        // We tell the fake JWT Tool: "If you read this string, tell us it belongs to User #1 and it is NOT expired."
        Jwt jwt = mock(Jwt.class);
        when(jwt.getUserId()).thenReturn(userId);
        when(jwt.isExpired()).thenReturn(false);

        Jwt newAccessToken = mock(Jwt.class);

        when(jwtService.parseToken(refreshTokenString)).thenReturn(jwt);
        // We tell the fake Database: "User #1 exists."
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // We tell the fake JWT Tool: "If asked, create a brand new token for User #1."
        when(jwtService.generateAccessToken(user)).thenReturn(newAccessToken);

        // Act
        Jwt result = authService.refreshAccessToken(refreshTokenString);

        // Assert
        assertThat(result).isEqualTo(newAccessToken);
    }

    /**
     * Test refresh failure.
     */
    @Test
    void refreshAccessToken_ShouldThrow_WhenTokenExpiredOrInvalid() {
        String token = "bad_token";
        // Case 1: parseToken returns null
        when(jwtService.parseToken(token)).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshAccessToken(token))
                .isInstanceOf(BadCredentialsException.class);
    }
}