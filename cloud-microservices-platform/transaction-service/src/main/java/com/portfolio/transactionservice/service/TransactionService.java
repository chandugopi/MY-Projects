package com.portfolio.transactionservice.service;

import com.portfolio.common.event.TransactionEvent;
import com.portfolio.transactionservice.dto.*;
import com.portfolio.transactionservice.entity.Transaction;
import com.portfolio.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transaction Service with Kafka event publishing.
 * Demonstrates event-driven architecture in microservices.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private static final String TRANSACTION_TOPIC = "transaction-events";

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        logger.info("Processing transfer from {} to {}: {}",
                request.getFromAccountNumber(), request.getToAccountNumber(), request.getAmount());

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.TRANSFER)
                .fromAccountNumber(request.getFromAccountNumber())
                .toAccountNumber(request.getToAccountNumber())
                .amount(request.getAmount())
                .userId(request.getUserId())
                .description(request.getDescription())
                .status(Transaction.TransactionStatus.INITIATED)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // In a real system, this would call the Account Service via REST or gRPC
        // For demo, we simulate the transfer and mark as completed
        savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        savedTransaction.setCompletedAt(LocalDateTime.now());
        savedTransaction = transactionRepository.save(savedTransaction);

        // Publish event to Kafka for notification service
        publishTransactionEvent(savedTransaction);

        logger.info("Transfer completed: {}", savedTransaction.getTransactionId());
        return mapToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        return mapToResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccount(String accountNumber) {
        List<Transaction> fromTransactions = transactionRepository.findByFromAccountNumber(accountNumber);
        List<Transaction> toTransactions = transactionRepository.findByToAccountNumber(accountNumber);

        fromTransactions.addAll(toTransactions);
        return fromTransactions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void publishTransactionEvent(Transaction transaction) {
        try {
            TransactionEvent event = TransactionEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .transactionId(transaction.getTransactionId())
                    .type(TransactionEvent.TransactionType.valueOf(transaction.getType().name()))
                    .fromAccountId(transaction.getFromAccountNumber())
                    .toAccountId(transaction.getToAccountNumber())
                    .amount(transaction.getAmount())
                    .currency(transaction.getCurrency())
                    .status(TransactionEvent.TransactionStatus.valueOf(transaction.getStatus().name()))
                    .userId(transaction.getUserId() != null ? transaction.getUserId().toString() : null)
                    .timestamp(LocalDateTime.now())
                    .description(transaction.getDescription())
                    .build();

            kafkaTemplate.send(TRANSACTION_TOPIC, transaction.getTransactionId(), event);
            logger.info("Published TransactionEvent: {}", transaction.getTransactionId());
        } catch (Exception e) {
            logger.warn("Failed to publish event to Kafka: {}", e.getMessage());
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType().name())
                .fromAccountNumber(transaction.getFromAccountNumber())
                .toAccountNumber(transaction.getToAccountNumber())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .status(transaction.getStatus().name())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
