package com.portfolio.accountservice.controller;

import com.portfolio.accountservice.dto.*;
import com.portfolio.accountservice.service.AccountService;
import com.portfolio.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account created", response));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByUser(
            @PathVariable Long userId) {
        List<AccountResponse> responses = accountService.getAccountsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<ApiResponse<AccountResponse>> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponse response = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<ApiResponse<AccountResponse>> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountResponse response = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", response));
    }
}
