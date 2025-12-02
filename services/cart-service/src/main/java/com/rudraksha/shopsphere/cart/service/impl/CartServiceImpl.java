package com.rudraksha.shopsphere.cart.service.impl;

import com.rudraksha.shopsphere.cart.api.CatalogClient;
import com.rudraksha.shopsphere.cart.api.InventoryClient;
import com.rudraksha.shopsphere.cart.dto.request.AddToCartRequest;
import com.rudraksha.shopsphere.cart.dto.request.UpdateCartItemRequest;
import com.rudraksha.shopsphere.cart.dto.response.CartItemResponse;
import com.rudraksha.shopsphere.cart.dto.response.CartResponse;
import com.rudraksha.shopsphere.cart.entity.Cart;
import com.rudraksha.shopsphere.cart.entity.CartItem;
import com.rudraksha.shopsphere.cart.repository.CartRepository;
import com.rudraksha.shopsphere.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        // Validate request
        if (request == null || request.getProductId() == null || request.getProductId().isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Fetch product from catalog service
        var product = catalogClient.getProduct(request.getProductId());
        if (product == null) {
            throw new IllegalArgumentException("Product not found with id: " + request.getProductId());
        }
        
        // Check inventory availability
        var stock = inventoryClient.checkStock(request.getProductId(), request.getQuantity());
        if (stock == null) {
            throw new IllegalStateException("Unable to check stock for product: " + request.getProductId());
        }
        if (!stock.inStock()) {
            throw new IllegalStateException("Product is out of stock or insufficient quantity available");
        }

        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId() != null && item.getProductId().equals(request.getProductId()))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            // Validate new quantity doesn't exceed stock
            var updatedStock = inventoryClient.checkStock(request.getProductId(), newQuantity);
            if (updatedStock == null || !updatedStock.inStock()) {
                throw new IllegalStateException("Cannot add more. Insufficient stock available for quantity: " + newQuantity);
            }
            
            item.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                .productId(product.id())
                .productName(product.name())
                .quantity(request.getQuantity())
                .price(product.price())
                .imageUrl(product.imageUrl())
                .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Added product {} (qty: {}) to cart for user {}", request.getProductId(), request.getQuantity(), userId);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(String userId, String productId, UpdateCartItemRequest request) {
        // Validate inputs
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (request == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
            .filter(i -> i.getProductId() != null && i.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

        // Check stock availability for new quantity
        var stock = inventoryClient.checkStock(productId, request.getQuantity());
        if (stock == null) {
            throw new IllegalStateException("Unable to check stock for product: " + productId);
        }
        if (!stock.inStock()) {
            throw new IllegalStateException("Insufficient stock available for quantity: " + request.getQuantity());
        }

        item.setQuantity(request.getQuantity());
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Updated cart item {} (qty: {}) for user {}", productId, request.getQuantity(), userId);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeFromCart(String userId, String productId) {
        // Validate input
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }

        Cart cart = getOrCreateCart(userId);

        boolean removed = cart.getItems().removeIf(item -> 
            item.getProductId() != null && item.getProductId().equals(productId)
        );
        if (!removed) {
            throw new IllegalArgumentException("Product not found in cart");
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        log.info("Removed product {} from cart for user {}", productId, userId);
        return mapToResponse(cart);
    }

    @Override
    public void clearCart(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cartRepository.delete(cart);
            log.info("Cleared cart for user {}", userId);
        });
    }

    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
            .orElseGet(() -> {
                Cart newCart = Cart.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(userId)
                    .items(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .build();
                return cartRepository.save(newCart);
            });
    }

    private CartResponse mapToResponse(Cart cart) {
        // Validate cart
        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }
        
        // Handle null items list
        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        var itemResponses = cart.getItems().stream()
            .filter(item -> item != null) // Filter null items
            .map(this::mapItemToResponse)
            .toList();

        BigDecimal totalPrice = itemResponses.stream()
            .filter(response -> response.getSubtotal() != null)
            .map(CartItemResponse::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
            .mapToInt(response -> response.getQuantity() != null ? response.getQuantity() : 0)
            .sum();

        return CartResponse.builder()
            .id(cart.getId())
            .userId(cart.getUserId())
            .items(itemResponses)
            .totalItems(totalItems)
            .totalPrice(totalPrice)
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }

    private CartItemResponse mapItemToResponse(CartItem item) {
        // Validate item
        if (item == null) {
            throw new IllegalArgumentException("Cart item cannot be null");
        }
        
        // Validate item fields
        if (item.getPrice() == null) {
            throw new IllegalArgumentException("Item price cannot be null");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be greater than 0");
        }

        BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
            .productId(item.getProductId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(subtotal)
            .imageUrl(item.getImageUrl())
            .build();
    }
}
