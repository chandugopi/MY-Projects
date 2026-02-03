package com.portfolio.enterprise.exception;

/**
 * Exception for invalid order state transitions.
 */
public class InvalidOrderStateException extends RuntimeException {

    private final String currentState;
    private final String targetState;

    public InvalidOrderStateException(String currentState, String targetState) {
        super(String.format("Cannot transition order from %s to %s", currentState, targetState));
        this.currentState = currentState;
        this.targetState = targetState;
    }

    public InvalidOrderStateException(String message) {
        super(message);
        this.currentState = null;
        this.targetState = null;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getTargetState() {
        return targetState;
    }
}
