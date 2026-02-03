package com.portfolio.enterprise.service;

import com.portfolio.enterprise.dto.*;
import com.portfolio.enterprise.entity.Order;
import com.portfolio.enterprise.entity.Order.OrderStatus;
import com.portfolio.enterprise.entity.Order.OrderType;
import com.portfolio.enterprise.entity.OrderItem;
import com.portfolio.enterprise.entity.User;
import com.portfolio.enterprise.exception.InvalidOrderStateException;
import com.portfolio.enterprise.exception.ResourceNotFoundException;
import com.portfolio.enterprise.repository.OrderRepository;
import com.portfolio.enterprise.repository.UserRepository;
import com.portfolio.enterprise.workflow.OrderStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderStateMachine stateMachine;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Order order;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-12345678")
                .user(user)
                .status(OrderStatus.PENDING)
                .type(OrderType.STANDARD)
                .totalAmount(BigDecimal.valueOf(100.00))
                .shippingAddress("123 Test St")
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50.00))
                .build();
        order.addItem(item);

        createRequest = CreateOrderRequest.builder()
                .userId(1L)
                .type(OrderType.STANDARD)
                .shippingAddress("123 Test St")
                .items(Arrays.asList(
                        OrderItemRequest.builder()
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(BigDecimal.valueOf(50.00))
                                .build()))
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrder_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.createOrder(createRequest);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found for order")
    void testCreateOrder_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        createRequest.setUserId(999L);

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(createRequest));
    }

    @Test
    @DisplayName("Should get order by ID")
    void testGetOrderById_Success() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should get orders by user ID")
    void testGetOrdersByUserId() {
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(order));

        List<OrderResponse> responses = orderService.getOrdersByUserId(1L);

        assertEquals(1, responses.size());
    }

    @Test
    @DisplayName("Should transition order status successfully")
    void testTransitionOrder_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateMachine.canTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.transitionOrder(1L, OrderStatus.CONFIRMED);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("Should throw exception for invalid transition")
    void testTransitionOrder_InvalidTransition() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateMachine.canTransition(OrderStatus.PENDING, OrderStatus.DELIVERED)).thenReturn(false);

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.transitionOrder(1L, OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("Should confirm order")
    void testConfirmOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(stateMachine.canTransition(OrderStatus.PENDING, OrderStatus.CONFIRMED)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.confirmOrder(1L);

        assertEquals(OrderStatus.CONFIRMED, response.getStatus());
    }

    @Test
    @DisplayName("Should cancel pending order")
    void testCancelOrder_Pending() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderResponse response = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling shipped order")
    void testCancelOrder_Shipped() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class,
                () -> orderService.cancelOrder(1L));
    }
}
