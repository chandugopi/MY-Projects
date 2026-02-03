package com.portfolio.accountservice.dto;

import com.portfolio.accountservice.entity.Account.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private AccountType type;

    @Positive(message = "Initial deposit must be positive")
    private BigDecimal initialDeposit;

    private String currency;
}
