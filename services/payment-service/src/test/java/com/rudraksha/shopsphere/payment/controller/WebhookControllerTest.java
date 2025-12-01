package com.rudraksha.shopsphere.payment.controller;

import com.rudraksha.shopsphere.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void handleStripeWebhook_Success() throws Exception {
        // Arrange
        String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test123\"}}}";
        String signature = "t=1234567890,v1=valid_signature";

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", signature))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhookEvent(payload, signature);
    }

    @Test
    void handleStripeWebhook_MissingSignature() throws Exception {
        // Arrange
        String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test123\"}}}";

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleStripeWebhook_EmptyPayload() throws Exception {
        // Arrange
        String payload = "";
        String signature = "t=1234567890,v1=valid_signature";

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", signature))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhookEvent(payload, signature);
    }
}
