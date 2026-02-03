package com.portfolio.notificationservice.kafka;

import com.portfolio.common.event.TransactionEvent;
import com.portfolio.common.event.UserRegisteredEvent;
import com.portfolio.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for processing events from other microservices.
 * Demonstrates event-driven architecture pattern.
 */
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void consumeUserRegistered(UserRegisteredEvent event) {
        logger.info("Received UserRegisteredEvent: {}", event.getUserId());
        notificationService.sendWelcomeNotification(event);
    }

    @KafkaListener(topics = "transaction-events", groupId = "notification-service")
    public void consumeTransaction(TransactionEvent event) {
        logger.info("Received TransactionEvent: {}", event.getTransactionId());
        notificationService.sendTransactionNotification(event);
    }
}
