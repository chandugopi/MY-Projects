package com.portfolio.enterprise.workflow;

import com.portfolio.enterprise.entity.Order.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Implementation of state machine for order workflow management.
 * Demonstrates:
 * - State design pattern
 * - Validation of state transitions
 * - Workflow rules encapsulation
 */
@Component
public class OrderStateMachineImpl implements OrderStateMachine {

    private final Map<OrderStatus, Set<OrderStatus>> allowedTransitions;

    public OrderStateMachineImpl() {
        this.allowedTransitions = new EnumMap<>(OrderStatus.class);
        initializeTransitions();
    }

    /**
     * Defines valid state transitions.
     * 
     * Workflow:
     * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
     * ↘ CANCELLED ← (from PENDING, CONFIRMED, PROCESSING)
     */
    private void initializeTransitions() {
        // PENDING can go to CONFIRMED or CANCELLED
        allowedTransitions.put(OrderStatus.PENDING,
                EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));

        // CONFIRMED can go to PROCESSING or CANCELLED
        allowedTransitions.put(OrderStatus.CONFIRMED,
                EnumSet.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED));

        // PROCESSING can go to SHIPPED or CANCELLED
        allowedTransitions.put(OrderStatus.PROCESSING,
                EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));

        // SHIPPED can only go to DELIVERED
        allowedTransitions.put(OrderStatus.SHIPPED,
                EnumSet.of(OrderStatus.DELIVERED));

        // DELIVERED and CANCELLED are terminal states
        allowedTransitions.put(OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class));
        allowedTransitions.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    @Override
    public boolean canTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowed = allowedTransitions.get(from);
        return allowed != null && allowed.contains(to);
    }

    @Override
    public Set<OrderStatus> getNextStates(OrderStatus current) {
        return allowedTransitions.getOrDefault(current, EnumSet.noneOf(OrderStatus.class));
    }

    @Override
    public boolean isTerminalState(OrderStatus status) {
        Set<OrderStatus> transitions = allowedTransitions.get(status);
        return transitions == null || transitions.isEmpty();
    }

    @Override
    public List<List<OrderStatus>> getTransitionPaths(OrderStatus from, OrderStatus to) {
        List<List<OrderStatus>> paths = new ArrayList<>();
        findPaths(from, to, new ArrayList<>(), paths, new HashSet<>());
        return paths;
    }

    private void findPaths(OrderStatus current, OrderStatus target,
            List<OrderStatus> currentPath, List<List<OrderStatus>> allPaths,
            Set<OrderStatus> visited) {
        if (visited.contains(current)) {
            return; // Avoid cycles
        }

        currentPath.add(current);
        visited.add(current);

        if (current == target) {
            allPaths.add(new ArrayList<>(currentPath));
        } else {
            for (OrderStatus next : getNextStates(current)) {
                findPaths(next, target, currentPath, allPaths, visited);
            }
        }

        currentPath.remove(currentPath.size() - 1);
        visited.remove(current);
    }
}
