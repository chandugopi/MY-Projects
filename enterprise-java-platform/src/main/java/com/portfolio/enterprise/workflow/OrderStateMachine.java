package com.portfolio.enterprise.workflow;

import com.portfolio.enterprise.entity.Order.OrderStatus;

import java.util.List;
import java.util.Set;

/**
 * Interface for order workflow state machine.
 * Extracted to enable mocking in tests.
 */
public interface OrderStateMachine {

    /**
     * Checks if a transition is valid.
     */
    boolean canTransition(OrderStatus from, OrderStatus to);

    /**
     * Gets all possible next states from current state.
     */
    Set<OrderStatus> getNextStates(OrderStatus current);

    /**
     * Checks if current state is a terminal state.
     */
    boolean isTerminalState(OrderStatus status);

    /**
     * Gets all valid transition paths from initial to target state.
     */
    List<List<OrderStatus>> getTransitionPaths(OrderStatus from, OrderStatus to);
}
