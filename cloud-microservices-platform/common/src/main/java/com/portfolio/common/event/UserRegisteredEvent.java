package com.portfolio.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka event for user registration notifications.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {

    private String eventId;
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredAt;
}
