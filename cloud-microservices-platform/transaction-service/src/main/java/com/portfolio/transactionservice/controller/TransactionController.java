package com.portfolio.transactionservice.controller;

import com.portfolio.common.dto.ApiResponse;
import com.portfolio.transactionservice.dto.*;
import com.portfolio.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponse.success("Transfer completed", response));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable String transactionId) {
        TransactionResponse response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByUser(
            @PathVariable Long userId) {
        List<TransactionResponse> responses = transactionService.getTransactionsByUser(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
        List<TransactionResponse> responses = transactionService.getTransactionsByAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
