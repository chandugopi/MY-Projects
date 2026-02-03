package com.portfolio.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kafka event for transaction notifications.
 * Published by Transaction Service, consumed by Notification Service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {

    private String eventId;
    private String transactionId;
    private TransactionType type;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private String userId;
    private String userEmail;
    private LocalDateTime timestamp;
    private String description;

    public enum TransactionType {
        TRANSFER, DEPOSIT, WITHDRAWAL
    }

    public enum TransactionStatus {
        INITIATED, PROCESSING, COMPLETED, FAILED
    }
}
