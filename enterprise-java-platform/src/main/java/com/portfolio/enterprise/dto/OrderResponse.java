package com.portfolio.enterprise.dto;

import com.portfolio.enterprise.entity.Order.OrderStatus;
import com.portfolio.enterprise.entity.Order.OrderType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Order responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String userEmail;
    private OrderStatus status;
    private OrderType type;
    private BigDecimal totalAmount;
    private String notes;
    private String shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
