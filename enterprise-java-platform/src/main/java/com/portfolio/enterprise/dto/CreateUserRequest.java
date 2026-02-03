package com.portfolio.enterprise.dto;

import com.portfolio.enterprise.entity.User.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for User creation requests.
 * Demonstrates input validation with Bean Validation annotations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 50, message = "First name must be at most 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be at most 50 characters")
    private String lastName;

    private UserRole role;
}
