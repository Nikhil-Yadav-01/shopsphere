package com.rudraksha.shopsphere.returns.service.impl;

import com.rudraksha.shopsphere.returns.dto.request.ApproveReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.CreateReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.RejectReturnRequest;
import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.returns.dto.response.RMAResponse;
import com.rudraksha.shopsphere.returns.entity.RMA;
import com.rudraksha.shopsphere.returns.entity.ReturnRequest;
import com.rudraksha.shopsphere.returns.exception.*;
import com.rudraksha.shopsphere.returns.mapper.ReturnMapper;
import com.rudraksha.shopsphere.returns.repository.RMARepository;
import com.rudraksha.shopsphere.returns.repository.ReturnRepository;
import com.rudraksha.shopsphere.returns.service.ReturnsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReturnsServiceImpl implements ReturnsService {

    private static final int RETURN_WINDOW_DAYS = 30;
    private static final int RMA_VALIDITY_DAYS = 60;
    private static final String RMA_PREFIX = "RMA";

    private final ReturnRepository returnRepository;
    private final RMARepository rmaRepository;
    private final ReturnMapper returnMapper;

    @Override
    public ReturnResponse createReturn(CreateReturnRequest request, UUID userId) {
        log.info("Creating return request for order: {} by user: {}", request.getOrderId(), userId);

        request.validate();

        if (!isReturnEligible(request.getOrderId())) {
            throw new ReturnWindowExpiredException(
                    "Return window has expired for order: " + request.getOrderId()
            );
        }

        String rmaNumber = generateRMANumber();

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .rmaNumber(rmaNumber)
                .reason(request.getReason())
                .description(request.getDescription())
                .refundAmount(BigDecimal.ZERO)
                .shippingCostDeductible(BigDecimal.ZERO)
                .requestedAt(LocalDateTime.now())
                .build();

        ReturnRequest saved = returnRepository.save(returnRequest);

        RMA rma = RMA.builder()
                .returnRequestId(saved.getId())
                .rmaNumber(rmaNumber)
                .expiryDate(LocalDateTime.now().plusDays(RMA_VALIDITY_DAYS))
                .isExpired(false)
                .build();

        rmaRepository.save(rma);

        log.info("Return request created with RMA: {}", rmaNumber);
        return returnMapper.toReturnResponse(saved);
    }

    @Override
    public ReturnResponse getReturnById(UUID returnId) {
        log.debug("Fetching return request: {}", returnId);
        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found: " + returnId));
        return returnMapper.toReturnResponse(returnRequest);
    }

    @Override
    public ReturnResponse getReturnByRmaNumber(String rmaNumber) {
        log.debug("Fetching return request by RMA: {}", rmaNumber);
        ReturnRequest returnRequest = returnRepository.findByRmaNumber(rmaNumber)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found with RMA: " + rmaNumber));
        return returnMapper.toReturnResponse(returnRequest);
    }

    @Override
    public Page<ReturnResponse> getUserReturns(UUID userId, Pageable pageable) {
        log.debug("Fetching returns for user: {} with pagination", userId);
        return returnRepository.findByUserId(userId, pageable)
                .map(returnMapper::toReturnResponse);
    }

    @Override
    public Page<ReturnResponse> getAllReturns(Pageable pageable) {
        log.debug("Fetching all returns with pagination");
        return returnRepository.findAll(pageable)
                .map(returnMapper::toReturnResponse);
    }

    @Override
    public ReturnResponse approveReturn(UUID returnId, ApproveReturnRequest request) {
        log.info("Approving return: {}", returnId);

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found: " + returnId));

        if (returnRequest.getApprovedAt() != null || returnRequest.getRejectedAt() != null) {
            throw new InvalidReturnStateException(
                    "Return has already been processed: " + returnId
            );
        }

        returnRequest.approve();
        returnRequest.setRefundAmount(request.getRefundAmount());
        if (request.getShippingCostDeductible() != null) {
            returnRequest.setShippingCostDeductible(request.getShippingCostDeductible());
        }

        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Return approved: {}", returnId);
        return returnMapper.toReturnResponse(saved);
    }

    @Override
    public ReturnResponse rejectReturn(UUID returnId, RejectReturnRequest request) {
        log.info("Rejecting return: {}", returnId);

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found: " + returnId));

        if (returnRequest.getApprovedAt() != null || returnRequest.getRejectedAt() != null) {
            throw new InvalidReturnStateException(
                    "Return has already been processed: " + returnId
            );
        }

        returnRequest.reject(request.getReason());
        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Return rejected: {}", returnId);
        return returnMapper.toReturnResponse(saved);
    }

    @Override
    public ReturnResponse markReturnAsReceived(UUID returnId) {
        log.info("Marking return as received: {}", returnId);

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found: " + returnId));

        if (returnRequest.getApprovedAt() == null) {
            throw new InvalidReturnStateException(
                    "Return must be approved before marking as received: " + returnId
            );
        }

        returnRequest.markAsReceived();
        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Return marked as received: {}", returnId);
        return returnMapper.toReturnResponse(saved);
    }

    @Override
    public ReturnResponse processRefund(UUID returnId, String transactionId) {
        log.info("Processing refund for return: {} with transaction: {}", returnId, transactionId);

        ReturnRequest returnRequest = returnRepository.findById(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("Return not found: " + returnId));

        if (returnRequest.getApprovedAt() == null) {
            throw new InvalidReturnStateException(
                    "Return must be approved before processing refund: " + returnId
            );
        }

        if (returnRequest.getRefundProcessedAt() != null) {
            throw new InvalidReturnStateException(
                    "Refund has already been processed: " + returnId
            );
        }

        BigDecimal refundAmount = returnRequest.getRefundAmount()
                .subtract(returnRequest.getShippingCostDeductible());

        returnRequest.processRefund(transactionId, refundAmount);
        ReturnRequest saved = returnRepository.save(returnRequest);
        log.info("Refund processed for return: {} with amount: {}", returnId, refundAmount);
        return returnMapper.toReturnResponse(saved);
    }

    @Override
    public RMAResponse getRMA(UUID returnId) {
        log.debug("Fetching RMA for return: {}", returnId);

        RMA rma = rmaRepository.findByReturnRequestId(returnId)
                .orElseThrow(() -> new ReturnNotFoundException("RMA not found for return: " + returnId));

        if (rma.hasExpired() && !rma.getIsExpired()) {
            rma.markAsExpired();
            rmaRepository.save(rma);
            throw new RMAExpiredException("RMA has expired: " + rma.getRmaNumber());
        }

        return returnMapper.toRMAResponse(rma);
    }

    @Override
    public boolean isReturnEligible(UUID orderId) {
        // TODO: Integrate with order service to get delivery date
        // For now, return true - will be implemented with order service integration
        return true;
    }

    private String generateRMANumber() {
        return RMA_PREFIX + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
