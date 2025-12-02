package com.rudraksha.shopsphere.cart.service;

import com.rudraksha.shopsphere.cart.api.CatalogClient;
import com.rudraksha.shopsphere.cart.api.InventoryClient;
import com.rudraksha.shopsphere.cart.dto.request.AddToCartRequest;
import com.rudraksha.shopsphere.cart.dto.request.UpdateCartItemRequest;
import com.rudraksha.shopsphere.cart.dto.response.CartResponse;
import com.rudraksha.shopsphere.cart.entity.Cart;
import com.rudraksha.shopsphere.cart.repository.CartRepository;
import com.rudraksha.shopsphere.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceEnhancedTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private InventoryClient inventoryClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private String userId;
    private String productId;
    private Cart cart;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();

        cart = Cart.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Test
    void testAddToCart_NullRequest() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, null));
    }

    @Test
    void testAddToCart_NullProductId() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(null)
                .quantity(1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_BlankProductId() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId("")
                .quantity(1)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_NullQuantity() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(null)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_InvalidQuantity() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_ProductNotFound() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(catalogClient.getProduct(productId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_StockCheckReturnsNull() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(1)
                .build();

        var product = new CatalogClient.ProductResponse(productId, "Test Product", "description",
                BigDecimal.valueOf(100), "image.jpg", "category", Map.of());

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(catalogClient.getProduct(productId)).thenReturn(product);
        when(inventoryClient.checkStock(productId, 1)).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_InsufficientStock() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(10)
                .build();

        var product = new CatalogClient.ProductResponse(productId, "Test Product", "description",
                BigDecimal.valueOf(100), "image.jpg", "category", Map.of());
        var stock = new InventoryClient.StockResponse(productId, 5, false, "warehouse1");

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(catalogClient.getProduct(productId)).thenReturn(product);
        when(inventoryClient.checkStock(productId, 10)).thenReturn(stock);

        assertThrows(IllegalStateException.class, () -> cartService.addToCart(userId, request));
    }

    @Test
    void testAddToCart_Success() {
        AddToCartRequest request = AddToCartRequest.builder()
                .productId(productId)
                .quantity(2)
                .build();

        var product = new CatalogClient.ProductResponse(productId, "Test Product", "description",
                BigDecimal.valueOf(100), "image.jpg", "category", Map.of());
        var stock = new InventoryClient.StockResponse(productId, 10, true, "warehouse1");

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(catalogClient.getProduct(productId)).thenReturn(product);
        when(inventoryClient.checkStock(productId, 2)).thenReturn(stock);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addToCart(userId, request);

        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testUpdateCartItem_NullProductId() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.updateCartItem(userId, null, request));
    }

    @Test
    void testUpdateCartItem_BlankProductId() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(5)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.updateCartItem(userId, "", request));
    }

    @Test
    void testUpdateCartItem_NullRequest() {
        assertThrows(IllegalArgumentException.class, () -> cartService.updateCartItem(userId, productId, null));
    }

    @Test
    void testUpdateCartItem_NullQuantity() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(null)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.updateCartItem(userId, productId, request));
    }

    @Test
    void testUpdateCartItem_InvalidQuantity() {
        UpdateCartItemRequest request = UpdateCartItemRequest.builder()
                .quantity(0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> cartService.updateCartItem(userId, productId, request));
    }

    @Test
    void testRemoveFromCart_NullProductId() {
        assertThrows(IllegalArgumentException.class, () -> cartService.removeFromCart(userId, null));
    }

    @Test
    void testRemoveFromCart_BlankProductId() {
        assertThrows(IllegalArgumentException.class, () -> cartService.removeFromCart(userId, ""));
    }
}
