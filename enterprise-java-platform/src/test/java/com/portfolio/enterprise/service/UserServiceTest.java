package com.portfolio.enterprise.service;

import com.portfolio.enterprise.dto.CreateUserRequest;
import com.portfolio.enterprise.dto.UserResponse;
import com.portfolio.enterprise.entity.User;
import com.portfolio.enterprise.entity.User.UserRole;
import com.portfolio.enterprise.exception.DuplicateResourceException;
import com.portfolio.enterprise.exception.ResourceNotFoundException;
import com.portfolio.enterprise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createRequest;
    private User user;

    @BeforeEach
    void setUp() {
        createRequest = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should create user successfully")
    void testCreateUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(createRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate username")
    void testCreateUser_DuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(createRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate email")
    void testCreateUser_DuplicateEmail() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(createRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by ID")
    void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Should get user by username")
    void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserByUsername("testuser");

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    @DisplayName("Should get all users")
    void testGetAllUsers() {
        User user2 = User.builder()
                .id(2L).username("user2").email("user2@example.com")
                .role(UserRole.USER).active(true).build();
        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertEquals(2, responses.size());
    }

    @Test
    @DisplayName("Should get active users")
    void testGetActiveUsers() {
        when(userRepository.findByActiveTrue()).thenReturn(Arrays.asList(user));

        List<UserResponse> responses = userService.getActiveUsers();

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).getActive());
    }

    @Test
    @DisplayName("Should deactivate user")
    void testDeactivateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            return u;
        });

        UserResponse response = userService.deactivateUser(1L);

        assertFalse(response.getActive());
        verify(userRepository).save(any(User.class));
    }
}
