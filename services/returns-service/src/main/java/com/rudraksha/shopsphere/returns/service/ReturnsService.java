package com.rudraksha.shopsphere.returns.service;

import com.rudraksha.shopsphere.returns.dto.request.ApproveReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.CreateReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.RejectReturnRequest;
import com.rudraksha.shopsphere.returns.dto.response.RMAResponse;
import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReturnsService {

    /**
     * Creates a new return request for a delivered order
     */
    ReturnResponse createReturn(CreateReturnRequest request, UUID userId);

    /**
     * Retrieves a return request by ID
     */
    ReturnResponse getReturnById(UUID returnId);

    /**
     * Retrieves a return request by RMA number
     */
    ReturnResponse getReturnByRmaNumber(String rmaNumber);

    /**
     * Lists returns for a user with pagination
     */
    Page<ReturnResponse> getUserReturns(UUID userId, Pageable pageable);

    /**
     * Lists all returns with pagination (admin)
     */
    Page<ReturnResponse> getAllReturns(Pageable pageable);

    /**
     * Approves a return request
     */
    ReturnResponse approveReturn(UUID returnId, ApproveReturnRequest request);

    /**
     * Rejects a return request
     */
    ReturnResponse rejectReturn(UUID returnId, RejectReturnRequest request);

    /**
     * Marks a return as received
     */
    ReturnResponse markReturnAsReceived(UUID returnId);

    /**
     * Processes refund for an approved return
     */
    ReturnResponse processRefund(UUID returnId, String transactionId);

    /**
     * Gets RMA details for a return
     */
    RMAResponse getRMA(UUID returnId);

    /**
     * Checks if return is eligible (within 30 days of delivery)
     */
    boolean isReturnEligible(UUID orderId);
}
