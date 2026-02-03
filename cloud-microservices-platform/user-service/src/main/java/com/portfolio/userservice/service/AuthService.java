package com.portfolio.userservice.service;

import com.portfolio.common.event.UserRegisteredEvent;
import com.portfolio.userservice.dto.*;
import com.portfolio.userservice.entity.User;
import com.portfolio.userservice.repository.UserRepository;
import com.portfolio.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final String USER_TOPIC = "user-events";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(Collections.singleton(User.Role.USER))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        logger.info("User registered with ID: {}", savedUser.getId());

        // Publish event to Kafka
        publishUserRegisteredEvent(savedUser);

        String token = jwtTokenProvider.generateTokenFromUsername(savedUser.getUsername());

        return AuthResponse.of(
                token,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("User logged in: {}", request.getUsername());

        return AuthResponse.of(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    private void publishUserRegisteredEvent(User user) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userId(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .registeredAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send(USER_TOPIC, user.getId().toString(), event);
            logger.info("Published UserRegisteredEvent for user: {}", user.getUsername());
        } catch (Exception e) {
            logger.warn("Failed to publish event to Kafka: {}", e.getMessage());
        }
    }
}
