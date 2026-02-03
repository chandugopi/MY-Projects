package com.portfolio.enterprise.service;

import com.portfolio.enterprise.dto.CreateUserRequest;
import com.portfolio.enterprise.dto.UserResponse;
import com.portfolio.enterprise.entity.User;
import com.portfolio.enterprise.exception.DuplicateResourceException;
import com.portfolio.enterprise.exception.ResourceNotFoundException;
import com.portfolio.enterprise.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for User operations.
 * Demonstrates:
 * - Transaction management
 * - DTO/Entity mapping
 * - Business validation
 * - SOLID: Single Responsibility Principle
 */
@Service
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user.
     */
    public UserResponse createUser(CreateUserRequest request) {
        logger.info("Creating user: {}", request.getUsername());

        // Check for duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : User.UserRole.USER)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User created with ID: {}", savedUser.getId());

        return mapToResponse(savedUser);
    }

    /**
     * Gets a user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    /**
     * Gets a user by username.
     */
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToResponse(user);
    }

    /**
     * Gets all users.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets active users.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveUsers() {
        return userRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets users by role.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deactivates a user.
     */
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setActive(false);
        User savedUser = userRepository.save(user);
        logger.info("User {} deactivated", id);

        return mapToResponse(savedUser);
    }

    /**
     * Maps User entity to UserResponse DTO.
     * Manual mapping - in production, use MapStruct.
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
