package com.portfolio.enterprise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Enterprise Order and Workflow Management System.
 * 
 * This application demonstrates:
 * - Layered architecture
 * - SOLID principles
 * - Design patterns (Factory, Singleton, Strategy, State)
 * - Global exception handling
 * - DTO/Entity separation
 * - Audit logging
 * 
 * Course: CSCI 6620 – Software Engineering
 * 
 * @author Portfolio Project
 * @version 1.0.0
 */
@SpringBootApplication
public class EnterpriseApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseApplication.class, args);
    }
}
