package com.rudraksha.shopsphere.returns.controller;

import com.rudraksha.shopsphere.returns.dto.request.ApproveReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.CreateReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.RejectReturnRequest;
import com.rudraksha.shopsphere.returns.dto.response.RMAResponse;
import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.returns.service.ReturnsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
@Slf4j
public class ReturnsController {

    private final ReturnsService returnsService;

    @PostMapping
    public ResponseEntity<ReturnResponse> createReturn(
            @Valid @RequestBody CreateReturnRequest request,
            Authentication authentication) {
        log.info("Creating return request for order: {}", request.getOrderId());

        UUID userId = UUID.fromString(authentication.getName());
        ReturnResponse response = returnsService.createReturn(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{returnId}")
    public ResponseEntity<ReturnResponse> getReturnById(@PathVariable UUID returnId) {
        log.debug("Fetching return: {}", returnId);
        ReturnResponse response = returnsService.getReturnById(returnId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rma/{rmaNumber}")
    public ResponseEntity<ReturnResponse> getReturnByRmaNumber(@PathVariable String rmaNumber) {
        log.debug("Fetching return by RMA: {}", rmaNumber);
        ReturnResponse response = returnsService.getReturnByRmaNumber(rmaNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<ReturnResponse>> getUserReturns(
            Authentication authentication,
            Pageable pageable) {
        log.debug("Fetching returns for user with pagination");

        UUID userId = UUID.fromString(authentication.getName());
        Page<ReturnResponse> response = returnsService.getUserReturns(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{returnId}/approve")
    public ResponseEntity<ReturnResponse> approveReturn(
            @PathVariable UUID returnId,
            @Valid @RequestBody ApproveReturnRequest request) {
        log.info("Approving return: {}", returnId);

        ReturnResponse response = returnsService.approveReturn(returnId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{returnId}/reject")
    public ResponseEntity<ReturnResponse> rejectReturn(
            @PathVariable UUID returnId,
            @Valid @RequestBody RejectReturnRequest request) {
        log.info("Rejecting return: {}", returnId);

        ReturnResponse response = returnsService.rejectReturn(returnId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{returnId}/mark-received")
    public ResponseEntity<ReturnResponse> markReturnAsReceived(@PathVariable UUID returnId) {
        log.info("Marking return as received: {}", returnId);

        ReturnResponse response = returnsService.markReturnAsReceived(returnId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{returnId}/process-refund")
    public ResponseEntity<ReturnResponse> processRefund(
            @PathVariable UUID returnId,
            @RequestParam String transactionId) {
        log.info("Processing refund for return: {}", returnId);

        ReturnResponse response = returnsService.processRefund(returnId, transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{returnId}/rma")
    public ResponseEntity<RMAResponse> getRMA(@PathVariable UUID returnId) {
        log.debug("Fetching RMA for return: {}", returnId);
        RMAResponse response = returnsService.getRMA(returnId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<Page<ReturnResponse>> getAllReturns(Pageable pageable) {
        log.debug("Fetching all returns for admin");
        Page<ReturnResponse> response = returnsService.getAllReturns(pageable);
        return ResponseEntity.ok(response);
    }
}
