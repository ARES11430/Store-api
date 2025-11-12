package com.amin.store.users;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /**
     * Test that registering a user successfully saves to the repo and returns a DTO.
     */
    @Test
    void registerUser_ShouldSaveUser_WhenEmailIsUnique() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setName("Test User");

        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setEmail("test@example.com");

        UserDto expectedDto = new UserDto(1L, "Test User", "test@example.com");

        // Mock behavior
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toDto(userEntity)).thenReturn(expectedDto);

        // Act
        UserDto result = userService.registerUser(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class)); // Verify save was called
    }

    /**
     * Test that registering with a duplicate email throws the correct exception.
     */
    @Test
    void registerUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        RegisterUserRequest request = new RegisterUserRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(DuplicateUserException.class);

        verify(userRepository, never()).save(any()); // Ensure we didn't try to save
    }

    /**
     * Test getting a user by ID.
     */
    @Test
    void getUser_ShouldReturnUser_WhenIdExists() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserDto userDto = new UserDto(userId, "Name", "email");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // Act
        UserDto result = userService.getUser(userId);

        // Assert
        assertThat(result.getId()).isEqualTo(userId);
    }
}