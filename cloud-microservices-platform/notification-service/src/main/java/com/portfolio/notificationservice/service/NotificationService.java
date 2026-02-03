package com.portfolio.notificationservice.service;

import com.portfolio.common.event.TransactionEvent;
import com.portfolio.common.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notification Service for sending emails, SMS, and push notifications.
 * In a production system, this would integrate with email providers (SendGrid,
 * SES)
 * and SMS gateways (Twilio).
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void sendWelcomeNotification(UserRegisteredEvent event) {
        logger.info("Sending welcome email to: {}", event.getEmail());

        // In production, this would send an actual email
        String message = String.format(
                "Welcome %s %s! Your account has been created successfully.",
                event.getFirstName(),
                event.getLastName());

        simulateSendEmail(event.getEmail(), "Welcome to Our Platform!", message);
        logger.info("Welcome notification sent to user: {}", event.getUserId());
    }

    public void sendTransactionNotification(TransactionEvent event) {
        logger.info("Sending transaction notification for: {}", event.getTransactionId());

        String subject = "Transaction " + event.getStatus();
        String message = buildTransactionMessage(event);

        if (event.getUserEmail() != null) {
            simulateSendEmail(event.getUserEmail(), subject, message);
        }

        logger.info("Transaction notification sent for: {}", event.getTransactionId());
    }

    private String buildTransactionMessage(TransactionEvent event) {
        return String.format(
                "Your %s of %s %s from account %s has been %s.%s",
                event.getType().name().toLowerCase(),
                event.getCurrency(),
                event.getAmount(),
                event.getFromAccountId(),
                event.getStatus().name().toLowerCase(),
                event.getDescription() != null ? " Notes: " + event.getDescription() : "");
    }

    private void simulateSendEmail(String to, String subject, String body) {
        // Simulate email sending - in production would use JavaMailSender or external
        // service
        logger.info("=== SIMULATED EMAIL ===");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Body: {}", body);
        logger.info("========================");
    }
}
