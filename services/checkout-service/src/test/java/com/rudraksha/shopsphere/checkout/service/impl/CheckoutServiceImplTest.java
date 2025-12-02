package com.rudraksha.shopsphere.checkout.service.impl;

import com.rudraksha.shopsphere.checkout.api.CatalogClient;
import com.rudraksha.shopsphere.checkout.dto.request.CheckoutRequest;
import com.rudraksha.shopsphere.checkout.dto.response.CheckoutResponse;
import com.rudraksha.shopsphere.checkout.saga.CheckoutSagaOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CheckoutServiceImpl.
 * Tests price calculation with catalog client integration and saga orchestration.
 * Verifies graceful fallback when catalog service is unavailable.
 */
@ExtendWith(MockitoExtension.class)
class CheckoutServiceImplTest {

    @Mock
    private CheckoutSagaOrchestrator sagaOrchestrator;

    @Mock
    private CatalogClient catalogClient;

    @InjectMocks
    private CheckoutServiceImpl checkoutService;

    private CheckoutRequest checkoutRequest;

    @BeforeEach
    void setUp() {
        checkoutRequest = new CheckoutRequest();
        checkoutRequest.setUserId("user-123");
        
        CheckoutRequest.CheckoutItem item1 = new CheckoutRequest.CheckoutItem();
        item1.setProductId("prod-1");
        item1.setQuantity(2);
        
        CheckoutRequest.CheckoutItem item2 = new CheckoutRequest.CheckoutItem();
        item2.setProductId("prod-2");
        item2.setQuantity(1);
        
        checkoutRequest.setItems(Arrays.asList(item1, item2));
    }

    @Test
    void processCheckout_Success_AllProductsFetched() {
        // Arrange
        when(sagaOrchestrator.executeCheckoutSaga(anyString(), any()))
                .thenReturn(true);
        
        CatalogClient.ProductResponse product1 = new CatalogClient.ProductResponse(
                "prod-1", "Product 1", "Desc", BigDecimal.valueOf(50.00), 
                "img1.jpg", "cat1", Map.of()
        );
        
        CatalogClient.ProductResponse product2 = new CatalogClient.ProductResponse(
                "prod-2", "Product 2", "Desc", BigDecimal.valueOf(75.00),
                "img2.jpg", "cat2", Map.of()
        );
        
        when(catalogClient.getProduct("prod-1")).thenReturn(product1);
        when(catalogClient.getProduct("prod-2")).thenReturn(product2);

        // Act
        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getUserId()).isEqualTo("user-123");
        // (50*2) + (75*1) = 175
        assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.valueOf(175.00));
        assertThat(response.getFinalAmount()).isEqualTo(BigDecimal.valueOf(175.00));
        assertThat(response.getPaymentUrl()).isNotNull();
    }

    @Test
    void processCheckout_Success_AllCatalogFails_UsesFallback() {
        // Arrange
        when(sagaOrchestrator.executeCheckoutSaga(anyString(), any()))
                .thenReturn(true);
        when(catalogClient.getProduct(anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act
        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        // items.size * 100 fallback = 2 * 100 = 200
        assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    void processCheckout_Success_PartialProductFetch_UsesFallback() {
        // Arrange
        when(sagaOrchestrator.executeCheckoutSaga(anyString(), any()))
                .thenReturn(true);
        
        CatalogClient.ProductResponse product1 = new CatalogClient.ProductResponse(
                "prod-1", "Product 1", "Desc", BigDecimal.valueOf(50.00),
                "img1.jpg", "cat1", Map.of()
        );
        
        when(catalogClient.getProduct("prod-1")).thenReturn(product1);
        when(catalogClient.getProduct("prod-2")).thenThrow(new RuntimeException("Product not found"));

        // Act
        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        // (50*2) + (1*100 fallback) = 200
        assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    void processCheckout_Failed_SagaExecution() {
        // Arrange
        when(sagaOrchestrator.executeCheckoutSaga(anyString(), any()))
                .thenReturn(false);

        CatalogClient.ProductResponse product1 = new CatalogClient.ProductResponse(
                "prod-1", "Product 1", "Desc", BigDecimal.valueOf(50.00),
                "img1.jpg", "cat1", Map.of()
        );
        
        when(catalogClient.getProduct(anyString())).thenReturn(product1);

        // Act
        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("FAILED");
        assertThat(response.getPaymentUrl()).isNull();
    }

    @Test
    void processCheckout_Success_NullProductResponse_UsesFallback() {
        // Arrange
        when(sagaOrchestrator.executeCheckoutSaga(anyString(), any()))
                .thenReturn(true);
        
        CatalogClient.ProductResponse product1 = new CatalogClient.ProductResponse(
                "prod-1", "Product 1", "Desc", BigDecimal.valueOf(50.00),
                "img1.jpg", "cat1", Map.of()
        );
        
        when(catalogClient.getProduct("prod-1")).thenReturn(product1);
        when(catalogClient.getProduct("prod-2")).thenReturn(null);

        // Act
        CheckoutResponse response = checkoutService.processCheckout(checkoutRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        // (50*2) + (1*100 fallback) = 200
        assertThat(response.getTotalAmount()).isEqualTo(BigDecimal.valueOf(200));
    }
}
