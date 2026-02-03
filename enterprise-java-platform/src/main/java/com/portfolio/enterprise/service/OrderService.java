package com.portfolio.enterprise.service;

import com.portfolio.enterprise.dto.*;
import com.portfolio.enterprise.entity.Order;
import com.portfolio.enterprise.entity.Order.OrderStatus;
import com.portfolio.enterprise.entity.OrderItem;
import com.portfolio.enterprise.entity.User;
import com.portfolio.enterprise.exception.InvalidOrderStateException;
import com.portfolio.enterprise.exception.ResourceNotFoundException;
import com.portfolio.enterprise.repository.OrderRepository;
import com.portfolio.enterprise.repository.UserRepository;
import com.portfolio.enterprise.workflow.OrderStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for Order operations.
 * Demonstrates:
 * - Complex business logic
 * - State machine integration
 * - Transaction management
 */
@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStateMachine stateMachine;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
            OrderStateMachine stateMachine) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.stateMachine = stateMachine;
    }

    /**
     * Creates a new order.
     */
    public OrderResponse createOrder(CreateOrderRequest request) {
        logger.info("Creating order for user: {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .type(request.getType() != null ? request.getType() : Order.OrderType.STANDARD)
                .notes(request.getNotes())
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .build();

        // Add items
        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .productName(itemRequest.getProductName())
                    .productCode(itemRequest.getProductCode())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .discount(itemRequest.getDiscount())
                    .build();
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created: {}", savedOrder.getOrderNumber());

        return mapToResponse(savedOrder);
    }

    /**
     * Gets an order by ID.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapToResponse(order);
    }

    /**
     * Gets an order by order number.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapToResponse(order);
    }

    /**
     * Gets all orders for a user.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets orders by status.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Transitions order to next status.
     * Uses state machine for validation.
     */
    public OrderResponse transitionOrder(Long orderId, OrderStatus targetStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus currentStatus = order.getStatus();

        if (!stateMachine.canTransition(currentStatus, targetStatus)) {
            throw new InvalidOrderStateException(currentStatus.name(), targetStatus.name());
        }

        order.setStatus(targetStatus);

        if (targetStatus == OrderStatus.DELIVERED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} transitioned from {} to {}",
                orderId, currentStatus, targetStatus);

        return mapToResponse(savedOrder);
    }

    /**
     * Confirms an order.
     */
    public OrderResponse confirmOrder(Long orderId) {
        return transitionOrder(orderId, OrderStatus.CONFIRMED);
    }

    /**
     * Cancels an order.
     */
    public OrderResponse cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException(
                    "Cannot cancel order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} cancelled", orderId);

        return mapToResponse(savedOrder);
    }

    /**
     * Generates a unique order number.
     */
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Maps Order entity to OrderResponse DTO.
     */
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser().getId())
                .userEmail(order.getUser().getEmail())
                .status(order.getStatus())
                .type(order.getType())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .shippingAddress(order.getShippingAddress())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }

    /**
     * Maps OrderItem entity to OrderItemResponse DTO.
     */
    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productName(item.getProductName())
                .productCode(item.getProductCode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discount(item.getDiscount())
                .subtotal(item.getSubtotal())
                .build();
    }
}
