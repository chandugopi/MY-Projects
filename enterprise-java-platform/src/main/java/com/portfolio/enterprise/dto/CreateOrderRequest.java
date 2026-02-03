package com.portfolio.enterprise.dto;

import com.portfolio.enterprise.entity.Order.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * DTO for Order creation requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private OrderType type;

    @Size(max = 500, message = "Notes must be at most 500 characters")
    private String notes;

    @NotBlank(message = "Shipping address is required")
    @Size(max = 500, message = "Shipping address must be at most 500 characters")
    private String shippingAddress;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<OrderItemRequest> items;
}
