package com.portfolio.accountservice.service;

import com.portfolio.accountservice.dto.*;
import com.portfolio.accountservice.entity.Account;
import com.portfolio.accountservice.repository.AccountRepository;
import com.portfolio.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        logger.info("Creating account for user: {}", request.getUserId());

        Account account = Account.builder()
                .userId(request.getUserId())
                .type(request.getType() != null ? request.getType() : Account.AccountType.CHECKING)
                .balance(request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .build();

        Account savedAccount = accountRepository.save(account);
        logger.info("Account created: {}", savedAccount.getAccountNumber());

        return mapToResponse(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUser(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse deposit(String accountNumber, BigDecimal amount) {
        logger.info("Depositing {} to account: {}", amount, accountNumber);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);

        logger.info("Deposit completed. New balance: {}", savedAccount.getBalance());
        return mapToResponse(savedAccount);
    }

    @Transactional
    public AccountResponse withdraw(String accountNumber, BigDecimal amount) {
        logger.info("Withdrawing {} from account: {}", amount, accountNumber);

        Account account = accountRepository.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", accountNumber));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account savedAccount = accountRepository.save(account);

        logger.info("Withdrawal completed. New balance: {}", savedAccount.getBalance());
        return mapToResponse(savedAccount);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .type(account.getType().name())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
