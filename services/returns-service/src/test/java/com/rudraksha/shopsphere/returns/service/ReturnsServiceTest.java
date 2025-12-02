package com.rudraksha.shopsphere.returns.service;

import com.rudraksha.shopsphere.returns.dto.request.ApproveReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.CreateReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.RejectReturnRequest;
import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.returns.entity.RMA;
import com.rudraksha.shopsphere.returns.entity.ReturnRequest;
import com.rudraksha.shopsphere.returns.exception.InvalidReturnStateException;
import com.rudraksha.shopsphere.returns.exception.ReturnNotFoundException;
import com.rudraksha.shopsphere.returns.mapper.ReturnMapper;
import com.rudraksha.shopsphere.returns.repository.RMARepository;
import com.rudraksha.shopsphere.returns.repository.ReturnRepository;
import com.rudraksha.shopsphere.returns.service.impl.ReturnsServiceImpl;
import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnsServiceTest {

    @Mock
    private ReturnRepository returnRepository;

    @Mock
    private RMARepository rmaRepository;

    @Mock
    private ReturnMapper returnMapper;

    @InjectMocks
    private ReturnsServiceImpl returnsService;

    private UUID returnId;
    private UUID orderId;
    private UUID userId;
    private ReturnRequest returnRequest;
    private ReturnResponse returnResponse;

    @BeforeEach
    void setUp() {
        returnId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        returnRequest = ReturnRequest.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.REQUESTED)
                .reason("Product defective")
                .refundAmount(BigDecimal.valueOf(100.00))
                .requestedAt(LocalDateTime.now())
                .build();

        returnResponse = ReturnResponse.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.REQUESTED)
                .reason("Product defective")
                .refundAmount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void testCreateReturn_Success() {
        CreateReturnRequest request = CreateReturnRequest.builder()
                .orderId(orderId)
                .reason("Product defective")
                .description("Item stopped working")
                .build();

        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);
        when(rmaRepository.save(any(RMA.class))).thenReturn(new RMA());
        when(returnMapper.toReturnResponse(any(ReturnRequest.class))).thenReturn(returnResponse);

        ReturnResponse response = returnsService.createReturn(request, userId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals(userId, response.getUserId());
        assertEquals(ReturnStatus.REQUESTED, response.getStatus());
        verify(returnRepository, times(1)).save(any(ReturnRequest.class));
        verify(rmaRepository, times(1)).save(any(RMA.class));
    }

    @Test
    void testCreateReturn_InvalidRequest() {
        CreateReturnRequest request = CreateReturnRequest.builder()
                .orderId(null)
                .reason("")
                .build();

        assertThrows(IllegalArgumentException.class, () -> returnsService.createReturn(request, userId));
    }

    @Test
    void testGetReturnById_Success() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));
        when(returnMapper.toReturnResponse(returnRequest)).thenReturn(returnResponse);

        ReturnResponse response = returnsService.getReturnById(returnId);

        assertNotNull(response);
        assertEquals(returnId, response.getId());
        verify(returnRepository, times(1)).findById(returnId);
    }

    @Test
    void testGetReturnById_NotFound() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.empty());

        assertThrows(ReturnNotFoundException.class, () -> returnsService.getReturnById(returnId));
    }

    @Test
    void testGetReturnByRmaNumber_Success() {
        when(returnRepository.findByRmaNumber("RMA-12345678")).thenReturn(Optional.of(returnRequest));
        when(returnMapper.toReturnResponse(returnRequest)).thenReturn(returnResponse);

        ReturnResponse response = returnsService.getReturnByRmaNumber("RMA-12345678");

        assertNotNull(response);
        assertEquals("RMA-12345678", response.getRmaNumber());
        verify(returnRepository, times(1)).findByRmaNumber("RMA-12345678");
    }

    @Test
    void testApproveReturn_Success() {
        ApproveReturnRequest request = ApproveReturnRequest.builder()
                .refundAmount(BigDecimal.valueOf(100.00))
                .shippingCostDeductible(BigDecimal.ZERO)
                .build();

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);
        when(returnMapper.toReturnResponse(any(ReturnRequest.class))).thenReturn(returnResponse);

        ReturnResponse response = returnsService.approveReturn(returnId, request);

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(100.00), response.getRefundAmount());
        verify(returnRepository, times(1)).findById(returnId);
        verify(returnRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void testApproveReturn_AlreadyProcessed() {
        ApproveReturnRequest request = ApproveReturnRequest.builder()
                .refundAmount(BigDecimal.valueOf(100.00))
                .build();

        returnRequest.setApprovedAt(LocalDateTime.now());
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));

        assertThrows(InvalidReturnStateException.class, () -> returnsService.approveReturn(returnId, request));
    }

    @Test
    void testRejectReturn_Success() {
        RejectReturnRequest request = RejectReturnRequest.builder()
                .reason("Return outside window")
                .build();

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);
        when(returnMapper.toReturnResponse(any(ReturnRequest.class))).thenReturn(returnResponse);

        ReturnResponse response = returnsService.rejectReturn(returnId, request);

        assertNotNull(response);
        verify(returnRepository, times(1)).findById(returnId);
        verify(returnRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void testMarkReturnAsReceived_Success() {
        returnRequest.setApprovedAt(LocalDateTime.now());

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);
        when(returnMapper.toReturnResponse(any(ReturnRequest.class))).thenReturn(returnResponse);

        ReturnResponse response = returnsService.markReturnAsReceived(returnId);

        assertNotNull(response);
        verify(returnRepository, times(1)).findById(returnId);
        verify(returnRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void testMarkReturnAsReceived_NotApproved() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));

        assertThrows(InvalidReturnStateException.class, () -> returnsService.markReturnAsReceived(returnId));
    }

    @Test
    void testProcessRefund_Success() {
        returnRequest.setApprovedAt(LocalDateTime.now());
        returnRequest.setRefundAmount(BigDecimal.valueOf(100.00));

        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));
        when(returnRepository.save(any(ReturnRequest.class))).thenReturn(returnRequest);
        when(returnMapper.toReturnResponse(any(ReturnRequest.class))).thenReturn(returnResponse);

        ReturnResponse response = returnsService.processRefund(returnId, "TXN-123456");

        assertNotNull(response);
        verify(returnRepository, times(1)).findById(returnId);
        verify(returnRepository, times(1)).save(any(ReturnRequest.class));
    }

    @Test
    void testProcessRefund_NotApproved() {
        when(returnRepository.findById(returnId)).thenReturn(Optional.of(returnRequest));

        assertThrows(InvalidReturnStateException.class, () -> returnsService.processRefund(returnId, "TXN-123456"));
    }
}
