package com.rudraksha.shopsphere.returns.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.returns.dto.request.ApproveReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.CreateReturnRequest;
import com.rudraksha.shopsphere.returns.dto.request.RejectReturnRequest;
import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.returns.service.ReturnsService;
import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReturnsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class ReturnsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReturnsService returnsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID returnId;
    private UUID orderId;
    private UUID userId;
    private ReturnResponse returnResponse;

    @BeforeEach
    void setUp() {
        returnId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userId = UUID.randomUUID();

        returnResponse = ReturnResponse.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.REQUESTED)
                .reason("Product defective")
                .refundAmount(BigDecimal.valueOf(100.00))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testCreateReturn_Success() throws Exception {
        CreateReturnRequest request = CreateReturnRequest.builder()
                .orderId(orderId)
                .reason("Product defective")
                .description("Item stopped working")
                .build();

        when(returnsService.createReturn(any(CreateReturnRequest.class), any(UUID.class)))
                .thenReturn(returnResponse);

        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(returnId.toString()))
                .andExpect(jsonPath("$.rmaNumber").value("RMA-12345678"))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @WithMockUser(username = "550e8400-e29b-41d4-a716-446655440000")
    void testCreateReturn_InvalidRequest() throws Exception {
        CreateReturnRequest request = CreateReturnRequest.builder()
                .orderId(null)
                .reason("")
                .build();

        mockMvc.perform(post("/api/returns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetReturnById_Success() throws Exception {
        when(returnsService.getReturnById(returnId)).thenReturn(returnResponse);

        mockMvc.perform(get("/api/returns/" + returnId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(returnId.toString()))
                .andExpect(jsonPath("$.rmaNumber").value("RMA-12345678"));
    }

    @Test
    @WithMockUser
    void testGetReturnByRmaNumber_Success() throws Exception {
        when(returnsService.getReturnByRmaNumber("RMA-12345678")).thenReturn(returnResponse);

        mockMvc.perform(get("/api/returns/rma/RMA-12345678")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rmaNumber").value("RMA-12345678"));
    }

    @Test
    @WithMockUser
    void testApproveReturn_Success() throws Exception {
        ApproveReturnRequest request = ApproveReturnRequest.builder()
                .refundAmount(BigDecimal.valueOf(100.00))
                .shippingCostDeductible(BigDecimal.ZERO)
                .build();

        ReturnResponse approvedResponse = ReturnResponse.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.APPROVED)
                .refundAmount(BigDecimal.valueOf(100.00))
                .build();

        when(returnsService.approveReturn(eq(returnId), any(ApproveReturnRequest.class)))
                .thenReturn(approvedResponse);

        mockMvc.perform(put("/api/returns/" + returnId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser
    void testRejectReturn_Success() throws Exception {
        RejectReturnRequest request = RejectReturnRequest.builder()
                .reason("Return outside window")
                .build();

        ReturnResponse rejectedResponse = ReturnResponse.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.REJECTED)
                .rejectionReason("Return outside window")
                .build();

        when(returnsService.rejectReturn(eq(returnId), any(RejectReturnRequest.class)))
                .thenReturn(rejectedResponse);

        mockMvc.perform(put("/api/returns/" + returnId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser
    void testProcessRefund_Success() throws Exception {
        ReturnResponse refundedResponse = ReturnResponse.builder()
                .id(returnId)
                .orderId(orderId)
                .userId(userId)
                .rmaNumber("RMA-12345678")
                .status(ReturnStatus.REFUNDED)
                .finalRefundAmount(BigDecimal.valueOf(100.00))
                .refundTransactionId("TXN-123456")
                .build();

        when(returnsService.processRefund(returnId, "TXN-123456"))
                .thenReturn(refundedResponse);

        mockMvc.perform(put("/api/returns/" + returnId + "/process-refund")
                .param("transactionId", "TXN-123456")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }
}
